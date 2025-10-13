# 🔧 故障排除指南

## 常见问题与解决方案

### 1. Channel is unrecoverably broken（通道损坏错误）

**错误信息**：
```
channel 'xxxxx' ~ Channel is unrecoverably broken and will be disposed!
```

**原因分析**：
这个错误通常与 Android 的渲染管道（Surface）或 IPC（进程间通信）有关，常见原因包括：

1. **ImageProxy 到 Bitmap 转换不正确** ✅ 已修复
   - RGBA_8888 格式的图像可能包含 row padding（行填充）
   - 直接复制 buffer 会导致图像数据损坏

2. **Bitmap 内存泄漏** ✅ 已修复
   - 未正确回收 Bitmap 导致内存耗尽
   - 多个 Bitmap 副本同时存在

3. **MediaPipe 图像处理异常** ✅ 已修复
   - 在 Bitmap 被回收后继续使用
   - 线程安全问题

**解决方案**：

#### ✅ 正确的 ImageProxy 转 Bitmap 实现

```kotlin
private fun ImageProxy.toBitmap(): Bitmap? {
    return try {
        val plane = planes[0]
        val buffer = plane.buffer
        buffer.rewind()

        // 考虑 row padding
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width

        val bitmap = Bitmap.createBitmap(
            width + rowPadding / pixelStride,
            height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)

        // 裁剪掉 padding
        if (rowPadding != 0) {
            val croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
            bitmap.recycle()
            croppedBitmap
        } else {
            bitmap
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error converting ImageProxy to Bitmap", e)
        null
    }
}
```

#### ✅ 正确的内存管理

```kotlin
private fun processImage(imageProxy: ImageProxy) {
    var bitmap: Bitmap? = null
    try {
        bitmap = imageProxy.toBitmap()
        if (bitmap == null) return

        // 处理 bitmap
        val processedBitmap = processBitmap(bitmap)

        // 原始 bitmap 不再需要，立即回收
        bitmap.recycle()
        bitmap = null

        // 传递 processedBitmap 给下一步
        doSomething(processedBitmap)

    } catch (e: Exception) {
        bitmap?.recycle()
    } finally {
        imageProxy.close()
    }
}
```

#### ✅ MediaPipe 内部管理 Bitmap 生命周期

```kotlin
fun detectAsync(bitmap: Bitmap, timestampMs: Long) {
    try {
        val mpImage = BitmapImageBuilder(bitmap).build()
        val result = handLandmarker?.detect(mpImage)
        // 处理结果...
    } finally {
        // 检测完成后立即回收
        if (!bitmap.isRecycled) {
            bitmap.recycle()
        }
    }
}
```

---

### 2. 应用闪退或崩溃

**检查清单**：

- [ ] 是否下载了 `hand_landmarker.task` 模型文件？
- [ ] 模型文件是否在正确位置（`app/src/main/assets/`）？
- [ ] 是否授予了相机权限？
- [ ] 设备是否支持 Android 7.0+？

**调试步骤**：

```bash
# 查看 Logcat
adb logcat | grep -E "(MainActivity|HandLandmarkerHelper|MediaPipe)"

# 检查崩溃日志
adb logcat | grep -E "AndroidRuntime|FATAL"

# 查看内存使用
adb shell dumpsys meminfo <package_name>
```

---

### 3. MediaPipe 初始化失败

#### 错误 A："doesn't have a slash in it"

**完整错误信息**：
```
com.google.mediapipe.framework.MediaPipeException: internal: ; RET_CHECK failure
(mediapipe/util/resource_util_android.cc:86) last_slash_idx != std::string::npos
hand_landmarker.task doesn't have a slash in it
```

**原因**：模型文件不存在或路径不正确

**解决方案**：

1. **检查模型文件是否存在**
   ```bash
   ls -lh app/src/main/assets/hand_landmarker.task
   # 应该显示约 7.5MB 的文件
   ```

2. **如果文件不存在，下载模型文件**
   ```bash
   cd app/src/main/assets

   # 使用 curl
   curl -L -o hand_landmarker.task "https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task"

   # 或使用 wget
   wget https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task
   ```

3. **验证文件完整性**
   ```bash
   # 检查文件大小（应该是 7-8 MB）
   ls -lh hand_landmarker.task

   # 检查文件类型
   file hand_landmarker.task
   # 输出应该是: hand_landmarker.task: data
   ```

#### 错误 B："Error initializing HandLandmarker"

**可能原因**：

1. **模型文件损坏**
   ```bash
   # 重新下载
   cd app/src/main/assets
   rm hand_landmarker.task
   curl -L -o hand_landmarker.task "https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task"
   ```

2. **GPU 初始化失败 (GL_INVALID_ENUM)**

   **完整错误**：
   ```
   [GL_INVALID_ENUM]: An unacceptable value is specified for an enumerated argument.
   Calculator::Open() for node "InferenceCalculator" failed
   ```

   **原因**：设备 GPU 不支持 MediaPipe 所需的 OpenGL 特性

   **解决方案**：✅ 已自动处理

   代码会自动尝试 GPU，失败后回退到 CPU：
   ```kotlin
   // 在 HandLandmarkerHelper.kt 中已实现
   val delegates = listOf(Delegate.GPU, Delegate.CPU)
   for (delegate in delegates) {
       try {
           // 尝试初始化...
       } catch (e: Exception) {
           // 失败则尝试下一个
       }
   }
   ```

   查看日志确认使用的模式：
   ```bash
   adb logcat | grep "HandLandmarkerHelper"
   # 应该看到：
   # HandLandmarkerHelper: Attempting to initialize with delegate: GPU
   # HandLandmarkerHelper: Failed to initialize with GPU: ...
   # HandLandmarkerHelper: Attempting to initialize with delegate: CPU
   # HandLandmarkerHelper: HandLandmarker initialized successfully with CPU
   ```

3. **手动强制使用 CPU**

   如果想始终使用 CPU（更稳定但较慢），修改 `HandLandmarkerHelper.kt:59`：
   ```kotlin
   // 修改前
   val delegates = listOf(Delegate.GPU, Delegate.CPU)

   // 修改后（只使用 CPU）
   val delegates = listOf(Delegate.CPU)
   ```

4. **Android 版本过低**
   - 确保设备运行 Android 7.0 (API 24) 或更高版本

---

### 4. 手部检测不准确

**优化建议**：

1. **光线条件**
   - 确保光线充足
   - 避免强烈背光
   - 使用均匀照明

2. **手部位置**
   - 手部完全在镜头内
   - 与摄像头保持 30-50cm 距离
   - 手掌朝向摄像头

3. **背景环境**
   - 背景简洁，避免杂乱
   - 避免与肤色相近的背景

4. **调整检测参数**

   修改 `HandLandmarkerHelper.kt`：
   ```kotlin
   val options = HandLandmarker.HandLandmarkerOptions.builder()
       .setMinHandDetectionConfidence(0.3f)  // 降低阈值（默认 0.5）
       .setMinTrackingConfidence(0.3f)
       .setMinHandPresenceConfidence(0.3f)
       .build()
   ```

---

### 5. FPS 过低（< 15 FPS）

**优化方案**：

1. **降低图像分辨率**
   ```kotlin
   imageAnalyzer = ImageAnalysis.Builder()
       .setTargetResolution(Size(640, 480))  // 降低分辨率
       .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
       .build()
   ```

2. **跳帧处理**
   ```kotlin
   private var frameSkipCounter = 0
   private val FRAME_SKIP = 2  // 每处理1帧，跳过2帧

   private fun processImage(imageProxy: ImageProxy) {
       frameSkipCounter++
       if (frameSkipCounter % (FRAME_SKIP + 1) != 0) {
           imageProxy.close()
           return
       }
       // 处理图像...
   }
   ```

3. **使用 GPU 加速**
   ```kotlin
   .setDelegate(Delegate.GPU)  // 确保使用 GPU
   ```

---

### 6. 内存占用过高

**监控内存**：

```bash
# 实时监控内存
adb shell dumpsys meminfo <package_name>

# 查看内存泄漏
adb shell am dumpheap <package_name> /data/local/tmp/heap.hprof
adb pull /data/local/tmp/heap.hprof
```

**优化措施**：

1. **及时回收 Bitmap**（已实现）
2. **限制图像分辨率**
3. **减少 Bitmap 副本数量**

---

### 7. 相机预览卡顿

**检查项**：

1. **使用 STRATEGY_KEEP_ONLY_LATEST**（已实现）
   ```kotlin
   .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
   ```

2. **检测处理时间过长**
   - 添加时间日志：
   ```kotlin
   val startTime = System.currentTimeMillis()
   handLandmarkerHelper.detectAsync(bitmap, timestamp)
   val elapsed = System.currentTimeMillis() - startTime
   Log.d(TAG, "Detection time: ${elapsed}ms")
   ```

---

### 8. 穴位位置不准确

**调整算法**：

修改 `CoordinateUtils.kt` 中的插值参数：

```kotlin
// 合谷穴：调整中点位置
fun calculateHegu(landmarks: HandLandmarks): PointF? {
    val indexMcp = landmarks.getLandmark(5) ?: return null
    val thumbIp = landmarks.getLandmark(3) ?: return null

    // 可以尝试不同的插值比例
    return interpolate(indexMcp, thumbIp, 0.5f)  // 默认中点
    // 或 return interpolate(indexMcp, thumbIp, 0.4f)  // 偏向食指
}

// 劳宫穴：调整插值参数
fun calculateLaogong(landmarks: HandLandmarks): PointF? {
    // 调整这里的 0.3f 参数
    val basePoint = interpolate(middleMcp, wrist, 0.3f)
    // ...
}
```

---

## 🆘 仍然无法解决？

1. **收集详细日志**：
   ```bash
   adb logcat -d > logcat.txt
   ```

2. **提供设备信息**：
   ```bash
   adb shell getprop ro.build.version.release  # Android 版本
   adb shell getprop ro.product.model          # 设备型号
   ```

3. **提交 Issue**：
   - 附上完整错误日志
   - 说明复现步骤
   - 提供设备信息

---

## 📚 相关资源

- [MediaPipe 官方文档](https://developers.google.com/mediapipe)
- [CameraX 文档](https://developer.android.com/training/camerax)
- [Android 性能优化指南](https://developer.android.com/topic/performance)

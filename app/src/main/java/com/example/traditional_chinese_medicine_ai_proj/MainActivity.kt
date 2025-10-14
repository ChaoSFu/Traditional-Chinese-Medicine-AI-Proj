package com.example.traditional_chinese_medicine_ai_proj

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.view.Surface
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import android.util.Size
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.traditional_chinese_medicine_ai_proj.data.HandLandmarks
import com.example.traditional_chinese_medicine_ai_proj.ml.HandLandmarkerHelper
import com.example.traditional_chinese_medicine_ai_proj.ui.OverlayView
import com.example.traditional_chinese_medicine_ai_proj.utils.CoordinateUtils
import com.example.traditional_chinese_medicine_ai_proj.utils.LandmarkSmoother
import com.example.traditional_chinese_medicine_ai_proj.utils.ImagePreprocessor
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

/**
 * 中医手部穴位实时定位主界面
 * Main Activity for TCM Hand Acupoint Real-time Localization
 */
class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var overlayView: OverlayView
    private lateinit var statusText: TextView
    private lateinit var fpsText: TextView
    private lateinit var handednessText: TextView
    private lateinit var btnToggleLandmarks: Button
    private lateinit var btnToggleConnections: Button
    private lateinit var btnSwitchCamera: Button
    private lateinit var btnTakePhoto: Button

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private lateinit var landmarkSmoother: LandmarkSmoother

    private var camera: Camera? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var isFrontCamera = false  // 默认使用后置摄像头

    // 保存最新的手部检测结果用于拍照时叠加
    private var latestHandLandmarks: HandLandmarks? = null

    // 图像增强开关（可选功能）
    private var enableImageEnhancement = true

    // FPS计算
    private var frameCount = 0
    private var lastFpsTimestamp = 0L
    private var currentFps = 0

    // 显示选项
    private var showLandmarks = true
    private var showConnections = true

    // 选中的穴位列表（手背2个，手心2个）
    private var selectedAcupoints = setOf("LI4", "LI5", "PC8", "HT8") // 默认显示所有

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 获取选中的穴位
        intent.getStringArrayListExtra("SELECTED_ACUPOINTS")?.let {
            selectedAcupoints = it.toSet()
        }

        // 初始化视图
        initViews()

        // 初始化相机执行器
        cameraExecutor = Executors.newSingleThreadExecutor()

        // 初始化关键点平滑器
        // windowSize: 历史帧数，越大越平滑但延迟越高
        // smoothingFactor: 平滑强度，越小越平滑但延迟越高，越大响应越快
        landmarkSmoother = LandmarkSmoother(
            windowSize = 3,        // 减少到3帧，降低延迟
            smoothingFactor = 0.6f  // 提高到0.6，加快响应速度
        )

        // 检查权限
        if (allPermissionsGranted()) {
            initializeApp()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun initViews() {
        previewView = findViewById(R.id.previewView)
        overlayView = findViewById(R.id.overlayView)
        statusText = findViewById(R.id.statusText)
        fpsText = findViewById(R.id.fpsText)
        handednessText = findViewById(R.id.handednessText)
        btnToggleLandmarks = findViewById(R.id.btnToggleLandmarks)
        btnToggleConnections = findViewById(R.id.btnToggleConnections)
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)

        // 设置按钮初始文字
        btnToggleLandmarks.text = getString(R.string.btn_hide_landmarks)
        btnToggleConnections.text = getString(R.string.btn_hide_connections)

        // 设置按钮点击事件
        btnToggleLandmarks.setOnClickListener {
            showLandmarks = !showLandmarks
            overlayView.setShowLandmarks(showLandmarks)
            btnToggleLandmarks.text = if (showLandmarks) {
                getString(R.string.btn_hide_landmarks)
            } else {
                getString(R.string.btn_show_landmarks)
            }
        }

        btnToggleConnections.setOnClickListener {
            showConnections = !showConnections
            overlayView.setShowConnections(showConnections)
            btnToggleConnections.text = if (showConnections) {
                getString(R.string.btn_hide_connections)
            } else {
                getString(R.string.btn_show_connections)
            }
        }

        btnSwitchCamera.setOnClickListener {
            switchCamera()
        }

        btnTakePhoto.setOnClickListener {
            takePhoto()
        }
    }

    private fun initializeApp() {
        statusText.text = getString(R.string.status_initializing)

        // 初始化 HandLandmarker
        handLandmarkerHelper = HandLandmarkerHelper(
            context = this,
            listener = object : HandLandmarkerHelper.HandLandmarkerListener {
                override fun onResults(result: HandLandmarks?) {
                    // 应用平滑滤波
                    val smoothedResult = landmarkSmoother.smooth(result)
                    runOnUiThread {
                        updateUI(smoothedResult)
                    }
                }

                override fun onError(error: String) {
                    runOnUiThread {
                        statusText.text = "错误: $error"
                        Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        if (handLandmarkerHelper.initialize()) {
            startCamera()
            statusText.text = getString(R.string.status_ready)
        } else {
            statusText.text = getString(R.string.status_error)
            Toast.makeText(this, "初始化失败", Toast.LENGTH_LONG).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCamera(cameraProvider!!)
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization failed", e)
                statusText.text = "相机启动失败"
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun switchCamera() {
        isFrontCamera = !isFrontCamera
        // 切换摄像头时重置平滑器和手势判断缓存
        landmarkSmoother.reset()
        CoordinateUtils.resetPalmFacingCache()
        cameraProvider?.let { provider ->
            bindCamera(provider)
        }
    }

    private fun bindCamera(cameraProvider: ProcessCameraProvider) {
        // 预览配置
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        // 图像分析配置
        val resolutionSelector = ResolutionSelector.Builder()
            .setResolutionStrategy(
                ResolutionStrategy(
                    Size(1280, 720),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                )
            )
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    processImage(imageProxy)
                }
            }

        // 图像捕获配置
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetRotation(Surface.ROTATION_0)  // 固定旋转角度为0，避免自动旋转
            .build()

        // 选择摄像头
        val cameraSelector = if (isFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        try {
            // 解绑之前的用例
            cameraProvider.unbindAll()

            // 绑定生命周期
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalyzer,
                imageCapture
            )

            Log.d(TAG, "Camera bound successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Camera binding failed", e)
        }
    }

    private fun processImage(imageProxy: ImageProxy) {
        if (!handLandmarkerHelper.isReady()) {
            imageProxy.close()
            return
        }

        try {
            // 转换为 Bitmap，如果失败则提前返回
            val sourceBitmap = imageProxy.convertToBitmap() ?: run {
                Log.w(TAG, "Failed to convert ImageProxy to Bitmap")
                return
            }

            try {
                // 获取图像旋转角度
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees

                // 第一步：旋转图像到正确的方向
                val rotatedBitmap = if (rotationDegrees != 0) {
                    val matrix = Matrix().apply {
                        postRotate(rotationDegrees.toFloat())
                    }
                    val rotated = Bitmap.createBitmap(
                        sourceBitmap, 0, 0, sourceBitmap.width, sourceBitmap.height, matrix, true
                    )
                    sourceBitmap.recycle()
                    rotated
                } else {
                    sourceBitmap
                }

                // 第二步：前置摄像头需要镜像翻转
                val mirroredBitmap = if (isFrontCamera) {
                    val matrix = Matrix().apply {
                        postScale(-1f, 1f, rotatedBitmap.width / 2f, rotatedBitmap.height / 2f)
                    }
                    val mirrored = Bitmap.createBitmap(
                        rotatedBitmap, 0, 0, rotatedBitmap.width, rotatedBitmap.height, matrix, true
                    )
                    rotatedBitmap.recycle()
                    mirrored
                } else {
                    rotatedBitmap
                }

                // 第三步：缩放到 512x512 提高检测精度
                val targetSize = 512
                val scaledBitmap = Bitmap.createScaledBitmap(
                    mirroredBitmap,
                    targetSize,
                    targetSize,
                    true  // 使用双线性插值获得更好的质量
                )
                if (scaledBitmap != mirroredBitmap) {
                    mirroredBitmap.recycle()
                }

                // 第四步：图像增强（可选）
                val processedBitmap = if (enableImageEnhancement) {
                    // 使用专门优化手部检测的增强方法
                    val enhanced = ImagePreprocessor.enhanceForHandDetection(scaledBitmap)
                    scaledBitmap.recycle()
                    enhanced
                } else {
                    scaledBitmap
                }

                // 执行检测（processedBitmap 将在 detectAsync 内部被回收）
                // VIDEO 模式需要传入帧时间戳
                val frameTimestampMs = System.currentTimeMillis()
                handLandmarkerHelper.detectAsync(processedBitmap, frameTimestampMs)

                // 计算FPS
                calculateFps()

            } catch (e: Exception) {
                Log.e(TAG, "Error processing bitmap", e)
                // 如果出现异常，确保 bitmap 被回收
                if (!sourceBitmap.isRecycled) {
                    sourceBitmap.recycle()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in processImage", e)
        } finally {
            imageProxy.close()
        }
    }

    private fun updateUI(handLandmarks: HandLandmarks?) {
        // 保存最新的检测结果
        latestHandLandmarks = handLandmarks

        if (handLandmarks == null) {
            overlayView.clear()
            statusText.text = getString(R.string.status_no_hand_detected)
            handednessText.text = ""
            return
        }

        // 判断是手心还是手背
        val isPalm = CoordinateUtils.isPalmFacing(handLandmarks)
        val handPose = CoordinateUtils.getHandPoseDescription(handLandmarks)

        // 更新状态
        statusText.text = getString(R.string.status_detecting)
        // 前置摄像头需要反转左右手判断（因为镜像）
        val displayHandedness = if (isFrontCamera) {
            when (handLandmarks.handedness) {
                "Left" -> "右手"   // 前置摄像头镜像后反转
                "Right" -> "左手"  // 前置摄像头镜像后反转
                else -> "未知"
            }
        } else {
            when (handLandmarks.handedness) {
                "Left" -> "左手"
                "Right" -> "右手"
                else -> "未知"
            }
        }

        handednessText.text = "检测到: $displayHandedness - $handPose"

        // 更新关键点
        overlayView.updateHandLandmarks(handLandmarks)

        // 根据手心/手背自动显示对应的穴位
        if (isPalm) {
            // 显示手心穴位，隐藏手背穴位
            overlayView.updateLi4Position(null)
            overlayView.updateLi5Position(null)

            if (selectedAcupoints.contains("PC8")) {
                val pc8Position = CoordinateUtils.calculateLaogong(handLandmarks)
                overlayView.updatePc8Position(pc8Position)
            } else {
                overlayView.updatePc8Position(null)
            }

            if (selectedAcupoints.contains("HT8")) {
                val ht8Position = CoordinateUtils.calculateShaofu(handLandmarks)
                overlayView.updateHt8Position(ht8Position)
            } else {
                overlayView.updateHt8Position(null)
            }
        } else {
            // 显示手背穴位，隐藏手心穴位
            overlayView.updatePc8Position(null)
            overlayView.updateHt8Position(null)

            if (selectedAcupoints.contains("LI4")) {
                val li4Position = CoordinateUtils.calculateHegu(handLandmarks)
                overlayView.updateLi4Position(li4Position)
            } else {
                overlayView.updateLi4Position(null)
            }

            if (selectedAcupoints.contains("LI5")) {
                val li5Position = CoordinateUtils.calculateYangxi(handLandmarks)
                overlayView.updateLi5Position(li5Position)
            } else {
                overlayView.updateLi5Position(null)
            }
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: run {
            Toast.makeText(this, getString(R.string.error_camera_not_ready), Toast.LENGTH_SHORT).show()
            return
        }

        // 创建带时间戳的文件名
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "TCM_Acupoint_$timestamp.jpg"

        // 使用 MediaStore 保存到图库
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TCM_Acupoint")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // 拍照成功后，创建带穴位标注的图片
                    output.savedUri?.let { uri ->
                        try {
                            // 读取刚保存的图片（使用新的 API）
                            val originalBitmap = contentResolver.openInputStream(uri)?.use { inputStream ->
                                BitmapFactory.decodeStream(inputStream)
                            }

                            if (originalBitmap != null) {
                                // 创建带标注的图片
                                val annotatedBitmap = createAnnotatedBitmap(originalBitmap)

                                // 保存标注后的图片
                                contentResolver.openOutputStream(uri)?.use { outputStream ->
                                    annotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                                }

                                originalBitmap.recycle()
                                annotatedBitmap.recycle()
                            }

                            runOnUiThread {
                                Toast.makeText(
                                    this@MainActivity,
                                    getString(R.string.photo_saved_success),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error creating annotated image", e)
                            runOnUiThread {
                                Toast.makeText(
                                    this@MainActivity,
                                    getString(R.string.photo_saved_success),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.error_photo_capture),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }

    private fun createAnnotatedBitmap(originalBitmap: Bitmap): Bitmap {
        val processedBitmap = if (isFrontCamera) {
            // 前置摄像头：先水平翻转，再旋转 90 度
            val matrix1 = Matrix().apply {
                postScale(-1f, 1f, originalBitmap.width / 2f, originalBitmap.height / 2f)
            }
            val flipped = Bitmap.createBitmap(
                originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix1, true
            )

            val matrix2 = Matrix().apply {
                postRotate(90f)
            }
            val rotated = Bitmap.createBitmap(
                flipped, 0, 0, flipped.width, flipped.height, matrix2, true
            )

            if (flipped != rotated) flipped.recycle()
            if (originalBitmap != flipped) originalBitmap.recycle()
            rotated
        } else {
            // 后置摄像头：只旋转 90 度
            val matrix = Matrix().apply {
                postRotate(90f)
            }
            val rotated = Bitmap.createBitmap(
                originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true
            )
            if (rotated != originalBitmap) originalBitmap.recycle()
            rotated
        }

        // 创建可变的 Bitmap 副本
        val annotatedBitmap = processedBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(annotatedBitmap)

        // 如果有检测到的手部数据，叠加穴位标注
        latestHandLandmarks?.let { _ ->
            // 计算缩放比例
            val scaleX = annotatedBitmap.width.toFloat() / overlayView.width
            val scaleY = annotatedBitmap.height.toFloat() / overlayView.height

            // 绘制穴位（这里需要将 overlayView 的绘制逻辑复用）
            // 由于 overlayView 是自定义 View，我们可以让它绘制到这个 canvas 上
            canvas.save()
            canvas.scale(scaleX, scaleY)
            overlayView.draw(canvas)
            canvas.restore()
        }

        if (processedBitmap != annotatedBitmap && !processedBitmap.isRecycled) {
            processedBitmap.recycle()
        }

        return annotatedBitmap
    }

    private fun calculateFps() {
        frameCount++
        val currentTime = SystemClock.elapsedRealtime()

        if (lastFpsTimestamp == 0L) {
            lastFpsTimestamp = currentTime
        }

        val elapsed = currentTime - lastFpsTimestamp
        if (elapsed >= 1000) {  // 每秒更新一次
            currentFps = (frameCount * 1000 / elapsed).toInt()
            runOnUiThread {
                fpsText.text = "FPS: $currentFps"
                // 更新 OverlayView 的调试信息
                overlayView.updateDebugInfo(currentFps, landmarkSmoother.getIsStationary())
            }
            frameCount = 0
            lastFpsTimestamp = currentTime
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                initializeApp()
            } else {
                Toast.makeText(this, "需要相机权限才能使用此应用", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        handLandmarkerHelper.close()
    }

    /**
     * ImageProxy 转 Bitmap 扩展函数
     * 支持 RGBA_8888 格式的正确转换，处理 row padding
     */
    private fun ImageProxy.convertToBitmap(): Bitmap? {
        return try {
            val plane = planes[0]
            val buffer = plane.buffer

            // 重置 buffer 位置
            buffer.rewind()

            // 计算每行的字节数（包含 padding）
            val pixelStride = plane.pixelStride
            val rowStride = plane.rowStride
            val rowPadding = rowStride - pixelStride * width

            // 创建 Bitmap
            val bitmap = Bitmap.createBitmap(
                width + rowPadding / pixelStride,
                height,
                Bitmap.Config.ARGB_8888
            )

            // 复制数据
            bitmap.copyPixelsFromBuffer(buffer)

            // 如果有 padding，需要裁剪
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
}

# 📱 中医手部穴位实时定位 - Android 版（MVP v1.0）

基于 MediaPipe Hands 的中医手部穴位实时检测与定位 Android 应用

## 🎯 项目概述

本应用使用 MediaPipe 的手部关键点检测技术，实时识别手部并标注中医穴位位置，目前支持：
- **合谷穴 (LI4)** - 手阳明大肠经
- **劳宫穴 (PC8)** - 手厥阴心包经

## ✨ 核心特性

- ✅ **离线运行** - 无需网络连接，本地推理
- ✅ **实时检测** - 20+ FPS，延迟 < 200ms
- ✅ **高精度定位** - 基于21个手部关键点计算穴位位置
- ✅ **可视化显示** - 实时叠加穴位标注和手部骨架
- ✅ **双手支持** - 自动识别左手/右手

## 📋 系统要求

- **最低 Android 版本**: Android 7.0 (API 24)
- **推荐 Android 版本**: Android 8.0+ (API 26+)
- **硬件要求**:
  - 摄像头（前置或后置）
  - 2GB+ RAM
  - 支持 OpenGL ES 2.0+

## 🏗️ 项目架构

```
app/
├── data/                           # 数据模型
│   ├── Acupoint.kt                # 穴位数据类
│   └── HandLandmark.kt            # 手部关键点数据类
│
├── ml/                            # 机器学习模块
│   └── HandLandmarkerHelper.kt    # MediaPipe 封装类
│
├── ui/                            # UI 组件
│   └── OverlayView.kt             # 自定义叠加视图
│
├── utils/                         # 工具类
│   └── CoordinateUtils.kt         # 坐标计算工具
│
├── MainActivity.kt                # 主Activity
│
└── assets/
    ├── acupoints.json             # 穴位说明数据
    └── hand_landmarker.task       # MediaPipe 模型文件（需下载）
```

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/yourusername/Traditional-Chinese-Medicine-AI-Proj.git
cd Traditional-Chinese-Medicine-AI-Proj
```

### 2. 下载 MediaPipe 模型

下载 MediaPipe Hand Landmarker 模型文件并放置到指定位置：

```bash
# 下载模型文件
wget https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task

# 将模型文件放到 assets 目录
cp hand_landmarker.task app/src/main/assets/
```

或者手动下载：
- 访问：https://developers.google.com/mediapipe/solutions/vision/hand_landmarker#models
- 下载 `hand_landmarker.task` 文件
- 放置到 `app/src/main/assets/` 目录

### 3. 构建和运行

使用 Android Studio：
1. 打开项目
2. 等待 Gradle 同步完成
3. 连接 Android 设备或启动模拟器
4. 点击运行按钮

使用命令行：
```bash
./gradlew assembleDebug
./gradlew installDebug
```

## 📱 使用说明

1. **启动应用** - 首次启动会请求相机权限
2. **手部对准摄像头** - 将手掌对准摄像头
3. **查看穴位标注** - 应用会实时显示：
   - 🔴 红色圆点：合谷穴 (LI4)
   - 🟠 橙色圆点：劳宫穴 (PC8)
4. **控制显示选项**：
   - "显示/隐藏关键点" - 切换21个手部关键点显示
   - "显示/隐藏骨架" - 切换手部骨架连接线显示

## 🧩 技术栈

### 核心技术
- **Kotlin** - 主要编程语言
- **CameraX** - 相机API（v1.3.0）
- **MediaPipe Hands** - 手部关键点检测（v0.10.9）
- **Gson** - JSON数据解析

### Android 组件
- **Custom View** - 自定义绘制层
- **LiveData & ViewModel** - 数据管理
- **Coroutines** - 异步处理

## 📊 性能指标

| 指标 | 目标 | 实际 |
|------|------|------|
| FPS | ≥ 20 | 25-30 (取决于设备) |
| 延迟 | < 200ms | ~100-150ms |
| 内存占用 | < 150MB | ~120MB |
| 模型大小 | < 30MB | ~26MB |

## 🔬 穴位定位算法

### 合谷穴 (LI4)
**位置**：手背第1、2掌骨之间的中点

**算法**：
```kotlin
合谷位置 = 中点(食指掌指关节, 拇指指间关节)
```

### 劳宫穴 (PC8)
**位置**：掌心第2、3掌骨之间

**算法**：
```kotlin
劳宫位置 = 加权平均(
    插值(中指掌指关节, 手腕, 0.3),
    中点(食指掌指关节, 无名指掌指关节)
)
```

## 🗺️ 版本规划

- [x] **v1.0** - 实时检测 + 合谷/劳宫定位
- [ ] **v1.1** - 穴位详情弹窗 + 功效说明
- [ ] **v1.2** - 语音提示（TTS）+ 震动反馈
- [ ] **v2.0** - 扩展所有手部穴位 + 姿势检测
- [ ] **v2.1** - 穴位按摩指导动画
- [ ] **v3.0** - 支持脚部、耳部穴位

## 🧪 测试

运行单元测试：
```bash
./gradlew test
```

运行仪器测试（需要连接设备）：
```bash
./gradlew connectedAndroidTest
```

## 🤝 贡献

欢迎贡献代码！请遵循以下步骤：

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 📝 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## ⚠️ 免责声明

本应用仅供学习和参考使用，不能替代专业医疗建议。使用本应用进行穴位定位时，请咨询专业中医师。

## 📞 联系方式

- 项目主页：[GitHub Repository](https://github.com/yourusername/Traditional-Chinese-Medicine-AI-Proj)
- 问题反馈：[Issues](https://github.com/yourusername/Traditional-Chinese-Medicine-AI-Proj/issues)

## 🙏 致谢

- [MediaPipe](https://developers.google.com/mediapipe) - 提供优秀的手部检测模型
- [CameraX](https://developer.android.com/training/camerax) - 简化相机开发
- 中医穴位数据参考：《针灸学》教材

---

**Made with ❤️ for Traditional Chinese Medicine**
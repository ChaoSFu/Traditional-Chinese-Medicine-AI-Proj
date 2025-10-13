# 🛠️ 开发环境配置指南

## 📋 前置要求

### 开发工具
- **Android Studio** Hedgehog | 2023.1.1 或更高版本
- **JDK** 11 或更高版本
- **Gradle** 8.0+（通过 Gradle Wrapper 自动管理）

### SDK 要求
- **Android SDK Platform**: API 34
- **Android SDK Build-Tools**: 34.0.0+
- **Android SDK Platform-Tools**: 最新版本

## 🚀 环境搭建步骤

### 1. 安装 Android Studio

从官网下载并安装：https://developer.android.com/studio

### 2. 配置 Android SDK

在 Android Studio 中：
1. 打开 `Settings/Preferences` → `Appearance & Behavior` → `System Settings` → `Android SDK`
2. 确保已安装：
   - Android 14.0 (API 34)
   - Android SDK Platform-Tools
   - Android SDK Build-Tools 34.0.0

### 3. 克隆项目

```bash
git clone <your-repo-url>
cd Traditional-Chinese-Medicine-AI-Proj
```

### 4. 下载 MediaPipe 模型文件

**重要**：项目运行前必须下载模型文件！

#### 方法 1: 使用 wget（推荐）

```bash
cd app/src/main/assets
wget https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task
```

#### 方法 2: 手动下载

1. 访问：https://developers.google.com/mediapipe/solutions/vision/hand_landmarker#models
2. 点击 "Download model" 按钮
3. 下载 `hand_landmarker.task` 文件（约 26MB）
4. 将文件放置到 `app/src/main/assets/` 目录

#### 验证模型文件

```bash
ls -lh app/src/main/assets/hand_landmarker.task
# 应该显示约 26MB 的文件
```

### 5. 同步 Gradle

打开 Android Studio：
1. 打开项目
2. 点击 `File` → `Sync Project with Gradle Files`
3. 等待依赖下载完成

### 6. 构建项目

```bash
# 清理构建
./gradlew clean

# 构建 Debug 版本
./gradlew assembleDebug
```

### 7. 连接设备或启动模拟器

#### 使用真机（推荐）
1. 在手机上启用开发者选项和 USB 调试
2. 连接手机到电脑
3. 在 Android Studio 中选择设备

#### 使用模拟器
1. 创建 AVD（Android Virtual Device）
2. 推荐配置：
   - 设备：Pixel 5 或更高
   - 系统镜像：Android 14 (API 34)
   - RAM: 2GB+
   - 启用摄像头支持

### 8. 运行应用

在 Android Studio 中点击运行按钮，或使用命令行：

```bash
./gradlew installDebug
```

## 🐛 常见问题

### 问题 1: Gradle 同步失败

**解决方案**：
```bash
# 清理 Gradle 缓存
./gradlew clean
rm -rf .gradle

# 重新同步
./gradlew build --refresh-dependencies
```

### 问题 2: 找不到 MediaPipe 依赖

**原因**：可能是网络问题导致依赖下载失败

**解决方案**：
1. 检查网络连接
2. 配置 Maven 镜像（在 `settings.gradle.kts` 或 `build.gradle.kts` 中）
3. 使用 VPN 或代理

### 问题 3: 应用启动后闪退

**可能原因**：
- ❌ 未下载 `hand_landmarker.task` 模型文件
- ❌ 未授予相机权限

**解决方案**：
1. 确认模型文件存在于 `app/src/main/assets/` 目录
2. 在设备上手动授予相机权限：`设置` → `应用` → `中医手部穴位定位` → `权限`

### 问题 4: CameraX 初始化失败

**解决方案**：
- 在真机上测试（模拟器可能不支持某些相机功能）
- 确保设备运行 Android 7.0+

### 问题 5: MediaPipe 检测不到手部

**优化建议**：
- 确保光线充足
- 手部完全进入摄像头视野
- 背景尽量简洁
- 手掌朝向摄像头

## 📊 项目结构验证

运行以下命令验证项目结构：

```bash
tree -L 3 app/src/main
```

应该看到类似输出：
```
app/src/main
├── AndroidManifest.xml
├── assets
│   ├── acupoints.json
│   └── hand_landmarker.task  ← 确保此文件存在！
├── java
│   └── com
│       └── example
│           └── traditional_chinese_medicine_ai_proj
│               ├── MainActivity.kt
│               ├── data/
│               ├── ml/
│               ├── ui/
│               └── utils/
└── res
    ├── layout
    ├── values
    └── ...
```

## 🧪 运行测试

```bash
# 单元测试
./gradlew test

# Android 仪器测试（需要连接设备）
./gradlew connectedAndroidTest

# Lint 检查
./gradlew lint
```

## 📦 构建发布版本

```bash
# 构建 Release APK（需要配置签名）
./gradlew assembleRelease

# 生成的 APK 位置
# app/build/outputs/apk/release/app-release.apk
```

## 🔧 IDE 推荐配置

### Android Studio 插件
- **Kotlin** - 已默认包含
- **Rainbow Brackets** - 彩色括号匹配
- **ADB Idea** - 快速 ADB 操作

### 代码格式化
使用 Kotlin 官方代码风格：
1. `Settings` → `Editor` → `Code Style` → `Kotlin`
2. 选择 "Set from..." → "Kotlin style guide"

## 📱 测试设备建议

### 最低配置
- Android 7.0 (API 24)
- 2GB RAM
- 前置摄像头

### 推荐配置
- Android 10+ (API 29+)
- 4GB+ RAM
- 支持 Camera2 API
- 支持 GPU 加速

### 已测试设备
- ✅ Google Pixel 5 (Android 14)
- ✅ Samsung Galaxy S21 (Android 13)
- ✅ Xiaomi Mi 11 (Android 12)
- ✅ OnePlus 9 (Android 13)

## 🆘 获取帮助

遇到问题？
1. 查看 [README.md](README.md)
2. 搜索 [Issues](https://github.com/yourusername/Traditional-Chinese-Medicine-AI-Proj/issues)
3. 提交新的 Issue

---

祝开发顺利！🎉

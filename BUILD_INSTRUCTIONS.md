# 🔨 构建说明

## ✅ 问题已修复

所有代码问题已修复，包括：
1. ✅ ImageProxy 转 Bitmap 转换
2. ✅ Bitmap 内存管理
3. ✅ MediaPipe 模型文件下载（已完成）
4. ✅ Kotlin 智能转换编译错误

## 🚀 构建方法

### 方法 1: 使用 Android Studio（推荐）

这是最简单的方法，Android Studio 自带 JDK 11+：

1. **打开项目**
   - 启动 Android Studio
   - 选择 "Open" 打开项目目录
   - 等待 Gradle 同步完成

2. **构建项目**
   - 点击菜单：`Build` → `Make Project`
   - 或使用快捷键：`Ctrl+F9` (Windows/Linux) 或 `Cmd+F9` (Mac)

3. **运行应用**
   - 连接 Android 设备或启动模拟器
   - 点击运行按钮（绿色三角形）
   - 或使用快捷键：`Shift+F10` (Windows/Linux) 或 `Ctrl+R` (Mac)

### 方法 2: 使用命令行

#### 前提条件

项目需要 **Java 11 或更高版本**。

检查当前 Java 版本：
```bash
java -version
```

如果显示 `java version "1.8.x"`，需要安装 Java 11+。

#### 安装 Java 11+ (macOS)

**选项 A: 使用 Homebrew**
```bash
# 安装 OpenJDK 11
brew install openjdk@11

# 设置为默认 Java
sudo ln -sfn $(brew --prefix)/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk

# 验证
java -version
```

**选项 B: 下载 Oracle JDK**
- 访问：https://www.oracle.com/java/technologies/downloads/
- 下载并安装 JDK 11 或更高版本

**选项 C: 使用 SDKMAN!**
```bash
# 安装 SDKMAN!
curl -s "https://get.sdkman.io" | bash

# 安装 Java 11
sdk install java 11.0.12-open

# 使用 Java 11
sdk use java 11.0.12-open
```

#### 构建命令

安装 Java 11+ 后：

```bash
# 清理项目
./gradlew clean

# 构建 Debug APK
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug

# 一键构建并安装
./gradlew clean assembleDebug installDebug
```

### 方法 3: 使用项目内置 JDK（如果有）

如果 Android Studio 已安装，可以使用其内置 JDK：

```bash
# 查找 Android Studio 内置 JDK
ls -la /Applications/Android\ Studio.app/Contents/jbr/Contents/Home/bin/java

# 设置 JAVA_HOME
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

# 验证
java -version

# 构建
./gradlew assembleDebug
```

## 📱 运行前检查清单

- [x] ✅ 模型文件已下载（`app/src/main/assets/hand_landmarker.task`，7.5MB）
- [x] ✅ Java 11+ 已安装（命令行构建需要）
- [ ] ⬜ 已连接 Android 设备或启动模拟器
- [ ] ⬜ 设备已启用 USB 调试
- [ ] ⬜ 设备运行 Android 7.0+

## 🔍 验证模型文件

```bash
# 检查文件是否存在
ls -lh app/src/main/assets/hand_landmarker.task

# 应该显示：
# -rw-r--r--  1 user  staff   7.5M Oct 13 16:59 hand_landmarker.task
```

如果文件不存在，运行：
```bash
./setup.sh
```

## 🐛 常见问题

### 问题 1: "Dependency requires at least JVM runtime version 11"

**原因**：系统 Java 版本低于 11

**解决方案**：
1. 安装 Java 11+（见上文）
2. 或使用 Android Studio 构建

### 问题 2: "Could not find hand_landmarker.task"

**原因**：模型文件未下载

**解决方案**：
```bash
./setup.sh
```

### 问题 3: "Smart cast to 'Bitmap' is impossible"

**状态**：✅ 已修复

已在 `MainActivity.kt` 中修复智能转换问题。

### 问题 4: 构建成功但运行时崩溃

**检查项**：
1. 查看 Logcat 日志
2. 确认模型文件存在
3. 确认已授予相机权限

```bash
# 查看日志
adb logcat | grep -E "(MainActivity|HandLandmarkerHelper)"
```

## 📊 构建输出

成功构建后，APK 文件位置：
```
app/build/outputs/apk/debug/app-debug.apk
```

## 🎯 下一步

构建成功后：
1. 安装到设备：`./gradlew installDebug`
2. 启动应用
3. 授予相机权限
4. 将手掌对准摄像头
5. 观察穴位标注

## 📚 相关文档

- [README.md](README.md) - 项目说明
- [SETUP_GUIDE.md](SETUP_GUIDE.md) - 环境配置
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - 故障排除

---

**如有问题，请查看 [TROUBLESHOOTING.md](TROUBLESHOOTING.md) 或提交 Issue。**

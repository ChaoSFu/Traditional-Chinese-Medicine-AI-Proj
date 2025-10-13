# ⚠️ 重要提示 - 必读

## 🚨 项目运行前必须完成的步骤

### 1. 下载 MediaPipe 模型文件

**该文件未包含在 Git 仓库中（约 26MB），必须手动下载！**

```bash
# 进入 assets 目录
cd app/src/main/assets

# 下载模型文件
wget https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task

# 或使用 curl
curl -O https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task
```

**验证下载**：
```bash
ls -lh hand_landmarker.task
# 应该显示约 26MB 的文件
```

### 2. 文件位置

确保模型文件位于正确位置：
```
app/src/main/assets/hand_landmarker.task
```

### 3. 为什么不包含在 Git 中？

- 文件较大（26MB），不适合放入版本控制
- MediaPipe 官方会定期更新模型
- 避免仓库体积过大

## ✅ 验证步骤

运行以下命令确认一切就绪：

```bash
# 检查模型文件
if [ -f "app/src/main/assets/hand_landmarker.task" ]; then
    echo "✅ 模型文件已就绪"
else
    echo "❌ 模型文件缺失！请先下载"
fi

# 检查文件大小
du -h app/src/main/assets/hand_landmarker.task
```

## 🎯 快速开始脚本

创建并运行此脚本自动下载模型：

```bash
#!/bin/bash
# setup.sh

echo "🚀 开始配置项目..."

# 创建 assets 目录（如果不存在）
mkdir -p app/src/main/assets

# 下载模型文件
echo "📥 下载 MediaPipe 模型文件..."
cd app/src/main/assets

if [ ! -f "hand_landmarker.task" ]; then
    wget https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task

    if [ $? -eq 0 ]; then
        echo "✅ 模型文件下载成功"
    else
        echo "❌ 下载失败，请手动下载"
        exit 1
    fi
else
    echo "✅ 模型文件已存在"
fi

cd ../../../..

# 同步 Gradle
echo "🔄 同步 Gradle 依赖..."
./gradlew clean build

echo "🎉 配置完成！现在可以运行项目了"
```

使用方法：
```bash
chmod +x setup.sh
./setup.sh
```

## 📚 更多信息

- 详细设置指南：[SETUP_GUIDE.md](SETUP_GUIDE.md)
- 项目说明：[README.md](README.md)
- MediaPipe 文档：https://developers.google.com/mediapipe

---

**如果您已完成上述步骤，就可以开始使用了！**

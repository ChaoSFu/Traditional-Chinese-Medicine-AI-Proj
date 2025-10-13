#!/bin/bash

# 中医手部穴位定位应用 - 快速设置脚本
# Traditional Chinese Medicine Hand Acupoint Localization - Quick Setup Script

set -e  # 遇到错误立即退出

echo "============================================="
echo "  中医手部穴位定位应用 - 自动设置"
echo "  TCM Hand Acupoint App - Auto Setup"
echo "============================================="
echo ""

# 检查是否在项目根目录
if [ ! -f "settings.gradle.kts" ]; then
    echo "❌ 错误：请在项目根目录运行此脚本"
    echo "   Error: Please run this script from the project root directory"
    exit 1
fi

# 创建 assets 目录（如果不存在）
echo "📁 创建 assets 目录..."
mkdir -p app/src/main/assets

# 检查模型文件
MODEL_FILE="app/src/main/assets/hand_landmarker.task"
MODEL_URL="https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task"

if [ -f "$MODEL_FILE" ]; then
    FILE_SIZE=$(ls -lh "$MODEL_FILE" | awk '{print $5}')
    echo "✅ 模型文件已存在: $MODEL_FILE ($FILE_SIZE)"
    read -p "是否重新下载？(y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "🗑️  删除旧文件..."
        rm "$MODEL_FILE"
    else
        echo "⏭️  跳过下载"
        MODEL_EXISTS=true
    fi
fi

if [ -z "$MODEL_EXISTS" ]; then
    echo "📥 下载 MediaPipe Hand Landmarker 模型文件..."
    echo "   URL: $MODEL_URL"
    echo "   目标: $MODEL_FILE"
    echo ""

    # 尝试使用 curl
    if command -v curl &> /dev/null; then
        echo "使用 curl 下载..."
        curl -L --progress-bar -o "$MODEL_FILE" "$MODEL_URL"
    # 尝试使用 wget
    elif command -v wget &> /dev/null; then
        echo "使用 wget 下载..."
        wget --show-progress -O "$MODEL_FILE" "$MODEL_URL"
    else
        echo "❌ 错误：未找到 curl 或 wget"
        echo "   请手动下载模型文件："
        echo "   $MODEL_URL"
        echo "   并保存到："
        echo "   $MODEL_FILE"
        exit 1
    fi

    # 验证下载
    if [ -f "$MODEL_FILE" ]; then
        FILE_SIZE=$(ls -lh "$MODEL_FILE" | awk '{print $5}')
        echo "✅ 模型文件下载成功！大小: $FILE_SIZE"
    else
        echo "❌ 模型文件下载失败"
        exit 1
    fi
fi

echo ""
echo "🔍 验证模型文件..."
ls -lh "$MODEL_FILE"

# 检查文件大小（应该大于 5MB）
FILE_SIZE_BYTES=$(wc -c < "$MODEL_FILE")
MIN_SIZE=$((5 * 1024 * 1024))  # 5 MB

if [ "$FILE_SIZE_BYTES" -lt "$MIN_SIZE" ]; then
    echo "⚠️  警告：文件大小小于预期（< 5MB），可能下载不完整"
    echo "   建议重新运行脚本"
    exit 1
fi

echo ""
echo "============================================="
echo "  ✅ 设置完成！"
echo "============================================="
echo ""
echo "📋 下一步："
echo "   1. 在 Android Studio 中打开项目"
echo "   2. 等待 Gradle 同步完成"
echo "   3. 连接 Android 设备或启动模拟器"
echo "   4. 点击运行按钮"
echo ""
echo "📚 文档："
echo "   - 项目说明: README.md"
echo "   - 设置指南: SETUP_GUIDE.md"
echo "   - 故障排除: TROUBLESHOOTING.md"
echo ""
echo "🎉 祝使用愉快！"
echo ""

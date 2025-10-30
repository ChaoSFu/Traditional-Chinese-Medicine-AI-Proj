# 🏥 中医智能健康管理系统

> 基于 AI 技术的综合性中医健康服务平台

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![MediaPipe](https://img.shields.io/badge/AI-MediaPipe-orange.svg)](https://developers.google.com/mediapipe)
[![OpenAI](https://img.shields.io/badge/AI-ChatGPT-412991.svg)](https://openai.com/)

## 📱 项目简介

结合现代 AI 技术与传统中医理论的智能健康管理系统，提供穴位识别、智能问答、医疗服务、社区交流等功能。

---

## ✨ 核心功能

### 🎯 AI 穴位识别
- **实时检测** - 基于 MediaPipe 的手部穴位定位
- **离线运行** - 本地推理，无需网络
- **双手支持** - 自动识别左右手、手心手背
- **支持穴位** - 合谷穴(LI4)、劳宫穴(PC8)

### 🤖 智能问答
- **ChatGPT 驱动** - 专业中医健康咨询
- **多轮对话** - 支持上下文理解
- **Markdown 渲染** - 清晰展示回答内容
- **对话历史** - 本地保存聊天记录

### 👨‍⚕️ 医疗服务
- **医生预约** - 科室浏览、医生排班、时段预约
- **就诊记录** - 完整病历查看和管理
- **病情上报** - 症状记录、图片上传、医生回复

### 🗣️ 交流社区
- **帖子互动** - 发布、评论、点赞
- **讲座活动** - 浏览、报名、精美配图
- **分类浏览** - 养生调理、药膳食疗、经络穴位等
- **社区统计** - 今日新帖、活跃用户、即将开讲

### 🎁 积分商城
- **积分获取** - 完成任务获得积分
- **等级系统** - 新手、进阶、达人、大师
- **商品兑换** - 课程、礼品、服务

### ✅ 每日任务
- **养生任务** - 晨起、午间、晚间、学习
- **积分奖励** - 完成任务获得积分
- **通知提醒** - 任务时间到达提醒

### 🏠 个性化首页
- **用户信息** - 体质、积分、等级
- **快捷入口** - 就诊记录、病情上报
- **社区动态** - 热门讨论、即将开始的讲座

---

## 🏗️ 技术栈

### 核心技术
- **Kotlin** + MVVM + Coroutines
- **Material Design 3** - 现代化 UI
- **CameraX** (1.3.0) - 相机 API
- **MediaPipe** (0.10.26.1) - 手部检测
- **Retrofit** (2.9.0) + OkHttp (4.12.0) - 网络请求
- **Room** (2.6.1) - 本地数据库
- **Glide** (4.16.0) - 图片加载
- **Markwon** (4.6.2) - Markdown 渲染

### 项目结构
```
app/src/main/java/.../traditional_chinese_medicine_ai_proj/
├── MainActivity.kt                  # 主入口（底部导航）
├── ui/fragments/                    # 7个Fragment（首页、预约、医生、记录、任务、商城、我的）
├── adapter/                         # RecyclerView适配器
├── data/                           # 数据模型
├── utils/                          # 工具类（Mock数据、积分管理、任务通知等）
├── ml/                             # MediaPipe封装
├── network/                        # ChatGPT API
├── database/                       # Room数据库
└── [Activity]                      # 各功能页面

assets/mock/                        # Mock数据（JSON格式）
assets/hand_landmarker.task         # MediaPipe模型
```

---

## 🚀 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/yourusername/Traditional-Chinese-Medicine-AI-Proj.git
cd Traditional-Chinese-Medicine-AI-Proj
```

### 2. 下载 MediaPipe 模型
下载 [hand_landmarker.task](https://developers.google.com/mediapipe/solutions/vision/hand_landmarker#models) 并放到 `app/src/main/assets/`

### 3. 配置 ChatGPT API（可选）
在应用"AI 问答"页面设置中配置你的 OpenAI API Key

### 4. 构建运行
```bash
./gradlew assembleDebug
./gradlew installDebug
```

或使用 Android Studio 直接运行

---

## 📱 使用指南

### 底部导航栏
- 🏠 **首页** - 个人信息、快捷入口、社区动态
- 📅 **预约** - 查看和管理预约
- 👨‍⚕️ **找医生** - 浏览科室、预约医生
- 📋 **记录** - 查看就诊记录
- ✅ **任务** - 每日任务打卡
- 🎁 **商城** - 积分兑换
- 👤 **我的** - 个人中心

### 主要功能流程

**穴位识别**
首页 → 手部穴位识别 → 允许相机权限 → 对准摄像头 → 查看穴位标注

**智能问答**
首页 → 智能问答 → 输入问题 → 查看 AI 回答

**医生预约**
找医生 → 选择科室 → 选择医生 → 查看排班 → 选择时段 → 确认预约

**参与社区**
首页 → 中医交流社区 → 浏览/发布帖子 → 查看讲座 → 报名活动

**完成任务**
任务 → 查看今日任务 → 点击完成 → 获得积分 → 商城兑换

---

## 📊 数据模型

| 类别 | 模型 | 说明 |
|------|------|------|
| 用户 | User, Appointment, MedicalRecord, Report | 用户信息、预约、就诊、上报 |
| 医疗 | Department, Doctor | 科室、医生信息 |
| 社区 | Post, Lecture | 帖子、讲座活动 |
| 商城 | PointsProduct, TodayTask | 商品、任务 |
| AI | ChatMessage, Acupoint | 聊天记录、穴位信息 |

---

## 📋 系统要求

- **最低版本**: Android 7.0 (API 24)
- **推荐版本**: Android 8.0+ (API 26+)
- **硬件**: 摄像头、2GB+ RAM

---

## 🗺️ 版本历史

### 当前版本 v0.3.0
- ✅ 医疗服务系统（预约、记录、上报）
- ✅ 中医交流社区（帖子、讲座）
- ✅ 积分商城和每日任务
- ✅ 底部导航栏整合
- ✅ 首页个性化

### 已完成
- v0.2.2 - MediaPipe 包更新
- v0.2.1 - Markdown 功能支持
- v0.2.0 - ChatGPT 智能问答
- v0.1.3 - 穴位识别稳定版
- v0.1.2 - 手心手背区分优化
- v0.1.0 - 手部穴位识别基础

### 计划中 v0.4.0+
- 用户注册登录
- 云端数据同步
- 真实医生在线咨询
- 支付功能
- 更多穴位（足部、耳部）
- 体质测评系统

---

## ⚠️ 免责声明

本应用仅供学习研究使用，不能替代专业医疗建议。如有健康问题，请咨询专业中医师并及时就医。

---

## 🙏 致谢

- [MediaPipe](https://developers.google.com/mediapipe) - 手部检测模型
- [OpenAI](https://openai.com/) - ChatGPT API
- [Unsplash](https://unsplash.com/) - 高质量图片
- 中医药大学公开课程和教材

---

## 📞 联系

- 项目主页: [GitHub](https://github.com/yourusername/Traditional-Chinese-Medicine-AI-Proj)
- 问题反馈: [Issues](https://github.com/yourusername/Traditional-Chinese-Medicine-AI-Proj/issues)

---

<div align="center">

**Made with ❤️ for Traditional Chinese Medicine**

*传承中医智慧，服务健康生活*

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

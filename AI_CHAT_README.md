# 中医智能问答模块使用说明

## 功能概述

中医智能问答模块是基于 ChatGPT API 开发的智能中医助手，用户可以通过文字输入中医相关问题，AI 会返回专业的中医知识解答。

## 主要功能

1. **智能问答**：支持中医穴位、经络、症状调理等问题咨询
2. **上下文对话**：支持多轮对话，AI 会记住对话历史（最近 20 条消息）
3. **实时响应**：使用 ChatGPT 3.5 Turbo 模型，响应快速
4. **离线缓存**：API Key 本地保存，无需每次输入

## 使用步骤

### 1. 获取 OpenAI API Key

1. 访问 [OpenAI Platform](https://platform.openai.com/api-keys)
2. 注册/登录账号
3. 创建新的 API Key
4. 复制保存 API Key（格式：sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx）

### 2. 配置 API Key 和模型

首次使用时，点击"中医智能问答"按钮后会提示输入 API Key。

**推荐方式**：通过设置页面完整配置
1. 在聊天界面点击右上角菜单 → 设置
2. 配置以下内容：
   - **API Key**：粘贴您的 OpenAI API Key
   - **AI 模型**：选择使用的模型
     - `gpt-3.5-turbo`（推荐）：速度快，费用低
     - `gpt-3.5-turbo-16k`：支持更长对话
     - `gpt-4`：更智能，理解能力更强
     - `gpt-4-turbo-preview`：GPT-4 优化版
     - `gpt-4o`：最新多模态模型
     - `gpt-4o-mini`：轻量版
   - **Temperature**（创造性）：0-2 之间，默认 0.7
     - 值越小：回答越严谨、一致
     - 值越大：回答越有创造性、多样化
   - **Max Tokens**（回答长度）：100-4000，默认 1000
     - 值越大：允许回答越长，但费用越高
3. 点击"保存设置"

**快捷方式**：首次使用弹窗配置（仅配置 API Key）

### 3. 开始问答

输入中医相关问题，例如：
- "手麻用哪些穴位调理？"
- "合谷穴的功效和位置是什么？"
- "劳宫配合哪个穴位治疗心烦？"
- "如何缓解颈部疼痛？"

AI 会基于中医理论给出专业回答，包括：
- 相关穴位推荐
- 穴位位置说明
- 功效解释
- 按摩建议
- 生活调理建议

### 4. 调整设置

随时可以通过设置页面调整：
- 更换 API Key
- 切换不同模型（根据需求和预算）
- 调整 Temperature（控制回答风格）
- 调整 Max Tokens（控制回答长度）
- 点击"恢复默认"重置所有高级参数

### 5. 清空对话

点击右上角菜单 → 清空对话，可清除所有历史消息。

## 技术架构

### 核心组件

```
ChatActivity          - 聊天界面 UI
ChatSettingsActivity  - 设置页面 UI
ChatViewModel         - 状态管理和业务逻辑
ChatAdapter           - 消息列表适配器
ChatGPTClient         - API 调用封装
OpenAIApi             - Retrofit API 接口
```

### 数据流

```
用户输入 → ChatActivity → ChatViewModel → ChatGPTClient
→ OpenAI API → AI 响应 → ChatViewModel → ChatActivity → UI 更新
```

### 依赖库

- **Retrofit 2.9.0**：网络请求
- **OkHttp 4.12.0**：HTTP 客户端
- **Gson**：JSON 解析
- **Lifecycle (ViewModel, LiveData)**：状态管理

## 系统提示词

AI 助手使用以下系统提示词，确保回答符合中医专业性：

```
You are an experienced Traditional Chinese Medicine (TCM) practitioner and assistant.
You answer questions about meridians, acupoints, and symptom treatment using classical TCM theory.
You can recommend acupoints and explain their effects in clear, human-friendly language.
If appropriate, combine advice on lifestyle, diet, and emotional regulation.
Always provide practical and safe recommendations.
Answer in Simplified Chinese (简体中文).
```

## 隐私和安全

- **API Key 本地存储**：使用 SharedPreferences 加密保存
- **不上传个人信息**：仅发送用户问题文本，不包含任何个人身份信息
- **对话历史本地**：对话记录仅保存在内存中，应用关闭后自动清除

## 费用说明

OpenAI API 按 Token 使用量计费，不同模型价格不同：

### 价格参考（2025）

| 模型 | 输入价格 | 输出价格 | 特点 |
|------|---------|---------|------|
| gpt-3.5-turbo | $0.0015/1K | $0.002/1K | 性价比高 ✅ |
| gpt-3.5-turbo-16k | $0.003/1K | $0.004/1K | 支持长文本 |
| gpt-4 | $0.03/1K | $0.06/1K | 最智能 |
| gpt-4-turbo | $0.01/1K | $0.03/1K | GPT-4 优化版 |
| gpt-4o | $0.005/1K | $0.015/1K | 多模态 |
| gpt-4o-mini | $0.00015/1K | $0.0006/1K | 超低价 ⚡ |

### 费用估算
- 1000 个汉字 ≈ 2000 Tokens
- 典型问答（50字问题 + 200字回答）：
  - GPT-3.5-turbo：约 $0.001
  - GPT-4o-mini：约 $0.0001
  - GPT-4：约 $0.01

## 常见问题

### Q: 提示 "请先配置 API Key"？
A: 点击确定后会弹出配置界面，输入您的 OpenAI API Key。

### Q: 提示 "请求失败：Unauthorized"？
A: API Key 无效或已过期，请重新配置正确的 API Key。

### Q: 提示 "请求失败：Network error"？
A: 检查网络连接，确保能访问 OpenAI API（可能需要特殊网络环境）。

### Q: 如何更换 API Key 或切换模型？
A: 点击右上角菜单 → 设置，可以更改 API Key、选择不同模型、调整参数。

### Q: 应该选择哪个模型？
A:
- **日常使用**：gpt-3.5-turbo（速度快、费用低）
- **预算极低**：gpt-4o-mini（超低价格）
- **追求质量**：gpt-4 或 gpt-4o（理解能力更强）
- **长对话**：gpt-3.5-turbo-16k（支持更长上下文）

### Q: 能离线使用吗？
A: 不能，AI 问答需要联网调用 ChatGPT API。穴位定位功能可离线使用。

## 扩展功能（未来计划）

- [ ] 语音输入支持
- [ ] TTS 语音播报
- [ ] 对话历史本地持久化
- [ ] 自定义 AI 模型选择（GPT-4）
- [ ] 穴位定位结果直接发送到 AI 问答
- [ ] 导出对话记录为 PDF

## 免责声明

本功能提供的中医知识仅供参考学习，不能替代专业医疗诊断和治疗。如有健康问题，请咨询专业医师。

---

**技术支持**：基于 OpenAI ChatGPT 3.5 Turbo
**版本**：v1.0
**更新时间**：2025-01

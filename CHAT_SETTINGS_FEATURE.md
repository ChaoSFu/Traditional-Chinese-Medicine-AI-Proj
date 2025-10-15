# AI 问答设置功能说明

## 新增功能概述

在中医智能问答模块中新增了完整的设置页面，用户可以自定义 AI 模型和参数。

## 功能特性

### 1. API Key 配置
- 安全的本地存储（SharedPreferences）
- 密码输入框保护隐私
- 提供官方获取链接提示

### 2. 模型选择
- 支持 6 种 OpenAI 模型：
  - `gpt-3.5-turbo`（默认，性价比高）
  - `gpt-3.5-turbo-16k`（支持长文本）
  - `gpt-4`（最智能）
  - `gpt-4-turbo-preview`（GPT-4 优化版）
  - `gpt-4o`（最新多模态）
  - `gpt-4o-mini`（超低价格）
- 下拉菜单选择，Material Design 风格

### 3. Temperature 参数
- 范围：0.0 - 2.0
- 默认：0.7
- 步进：0.1
- 实时显示当前值
- 提示说明：值越大越有创造性

### 4. Max Tokens 参数
- 范围：100 - 4000
- 默认：1000
- 步进：100
- 实时显示当前值
- 提示说明：值越大回答越长但费用越高

### 5. 快捷操作
- **保存设置**：保存所有配置到本地
- **恢复默认**：一键重置高级参数（不影响 API Key）

## 文件清单

### 新增文件
1. `ChatSettingsActivity.kt` - 设置页面 Activity
2. `activity_chat_settings.xml` - 设置页面布局
3. `CHAT_SETTINGS_FEATURE.md` - 本说明文档

### 修改文件
1. `ChatActivity.kt` - 添加跳转到设置页面
2. `ChatViewModel.kt` - 从设置中读取参数
3. `ChatGPTClient.kt` - 支持可配置参数
4. `AndroidManifest.xml` - 注册 ChatSettingsActivity
5. `strings.xml` - 添加相关字符串
6. `AI_CHAT_README.md` - 更新使用文档

## 使用流程

```
用户点击菜单"设置"
  ↓
进入 ChatSettingsActivity
  ↓
配置 API Key / 选择模型 / 调整参数
  ↓
点击"保存设置"
  ↓
配置保存到 SharedPreferences
  ↓
返回聊天界面
  ↓
ChatViewModel 从设置读取参数
  ↓
使用用户配置的参数调用 ChatGPT API
```

## 配置存储

### SharedPreferences Key
```kotlin
chat_settings {
    "api_key": String          // API Key
    "model": String            // 模型名称
    "temperature": Float       // 创造性参数
    "max_tokens": Int          // 最大 Token 数
}
```

### 默认值
```kotlin
DEFAULT_MODEL = "gpt-3.5-turbo"
DEFAULT_TEMPERATURE = 0.7f
DEFAULT_MAX_TOKENS = 1000
```

## UI 设计

### 设置页面布局结构
```
MaterialToolbar（顶部导航栏）
  ↓
ScrollView
  ├─ API 配置卡片
  │   └─ TextInputEditText（API Key）
  ├─ 模型设置卡片
  │   └─ AutoCompleteTextView（模型下拉）
  ├─ 高级参数卡片
  │   ├─ Temperature Slider
  │   └─ Max Tokens Slider
  ├─ 保存设置按钮
  └─ 恢复默认按钮
```

### 设计规范
- 遵循 Material Design 3
- 使用 Material Card 组织内容
- Slider 实时显示数值
- 每个设置项都有解释提示

## 技术实现

### 1. 模型下拉列表
```kotlin
val adapter = ArrayAdapter(
    this,
    android.R.layout.simple_dropdown_item_1line,
    AVAILABLE_MODELS
)
spinnerModel.setAdapter(adapter)
```

### 2. Slider 监听
```kotlin
sliderTemperature.addOnChangeListener { _, value, _ ->
    tvTemperatureValue.text = String.format("%.1f", value)
}
```

### 3. 配置保存
```kotlin
prefs.edit().apply {
    putString("api_key", apiKey)
    putString("model", model)
    putFloat("temperature", temperature)
    putInt("max_tokens", maxTokens)
    apply()
}
```

### 4. ChatViewModel 读取配置
```kotlin
val prefs = context.getSharedPreferences("chat_settings", Context.MODE_PRIVATE)
val model = prefs.getString("model", "gpt-3.5-turbo") ?: "gpt-3.5-turbo"
val temperature = prefs.getFloat("temperature", 0.7f)
val maxTokens = prefs.getInt("max_tokens", 1000)
```

## 扩展建议

### 未来可以添加的功能
1. **自定义系统提示词**：允许用户编辑 AI 角色定义
2. **对话历史长度设置**：控制保留多少条历史消息
3. **API 基础 URL 配置**：支持其他兼容 OpenAI API 的服务
4. **预设模板**：快捷选择"精准模式""创意模式"等
5. **费用统计**：显示累计使用的 Token 数和预估费用
6. **多账号管理**：支持保存多个 API Key 快速切换

## 注意事项

1. **API Key 安全**：
   - 使用密码输入框隐藏显示
   - 本地加密存储
   - 不要在日志中打印

2. **参数范围校验**：
   - Temperature: 0.0 - 2.0
   - Max Tokens: 100 - 4000
   - 超出范围会影响 API 调用

3. **费用控制**：
   - 建议设置 Max Tokens 上限
   - 使用 gpt-3.5-turbo 降低成本
   - 对话历史限制在 20 条以内

4. **用户体验**：
   - 提供清晰的参数说明
   - 显示实时数值反馈
   - 支持一键恢复默认

## 测试建议

1. **功能测试**：
   - 测试各个模型是否能正常工作
   - 测试参数调整后的效果差异
   - 测试恢复默认功能

2. **边界测试**：
   - 空 API Key
   - 极端参数值（0, 2, 100, 4000）
   - 无效的模型名称

3. **持久化测试**：
   - 保存后退出应用重新打开
   - 多次修改保存
   - 清除应用数据后的默认值

---

**版本**：v1.0
**完成时间**：2025-01
**开发者**：Traditional Chinese Medicine AI Team

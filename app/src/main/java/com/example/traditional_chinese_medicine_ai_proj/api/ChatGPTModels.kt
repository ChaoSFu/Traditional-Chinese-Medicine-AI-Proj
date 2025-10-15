package com.example.traditional_chinese_medicine_ai_proj.api

import com.google.gson.annotations.SerializedName

/**
 * ChatGPT API 请求数据模型
 */
data class ChatRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>,
    val temperature: Float = 0.7f,
    @SerializedName("max_tokens")
    val maxTokens: Int = 1000
)

/**
 * 消息数据模型
 */
data class Message(
    val role: String,  // "system", "user", "assistant"
    val content: String
)

/**
 * ChatGPT API 响应数据模型
 */
data class ChatResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage?
)

/**
 * 选择项
 */
data class Choice(
    val index: Int,
    val message: Message,
    @SerializedName("finish_reason")
    val finishReason: String?
)

/**
 * Token 使用统计
 */
data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)

/**
 * UI层消息模型
 */
data class MessageUI(
    val role: String,  // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

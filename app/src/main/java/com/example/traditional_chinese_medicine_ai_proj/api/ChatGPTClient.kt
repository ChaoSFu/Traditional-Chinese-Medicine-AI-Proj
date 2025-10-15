package com.example.traditional_chinese_medicine_ai_proj.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * ChatGPT API 客户端封装
 */
object ChatGPTClient {

    private const val BASE_URL = "https://api.openai.com/"

    // 系统提示词：定义 AI 助手的角色和行为
    const val SYSTEM_PROMPT = """You are an experienced Traditional Chinese Medicine (TCM) practitioner and assistant.
You answer questions about meridians, acupoints, and symptom treatment using classical TCM theory.
You can recommend acupoints and explain their effects in clear, human-friendly language.
If appropriate, combine advice on lifestyle, diet, and emotional regulation.
Always provide practical and safe recommendations.
Answer in Simplified Chinese (简体中文)."""

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: OpenAIApi = retrofit.create(OpenAIApi::class.java)

    /**
     * 发送消息到 ChatGPT
     * @param apiKey OpenAI API Key
     * @param userMessage 用户消息
     * @param conversationHistory 对话历史（可选）
     * @param model AI 模型（默认 gpt-3.5-turbo）
     * @param temperature 创造性参数（0-2，默认 0.7）
     * @param maxTokens 最大 token 数（默认 1000）
     * @return AI 回复内容
     */
    suspend fun sendMessage(
        apiKey: String,
        userMessage: String,
        conversationHistory: List<Message> = emptyList(),
        model: String = "gpt-3.5-turbo",
        temperature: Float = 0.7f,
        maxTokens: Int = 1000
    ): String {
        // 构建消息列表
        val messages = mutableListOf<Message>()

        // 添加系统提示词
        messages.add(Message("system", SYSTEM_PROMPT))

        // 添加对话历史（保留最近10轮对话）
        messages.addAll(conversationHistory.takeLast(20))

        // 添加当前用户消息
        messages.add(Message("user", userMessage))

        // 构建请求
        val request = ChatRequest(
            model = model,
            messages = messages,
            temperature = temperature,
            maxTokens = maxTokens
        )

        // 发送请求
        val response = api.chat("Bearer $apiKey", request)

        // 返回 AI 回复
        return response.choices.firstOrNull()?.message?.content
            ?: throw Exception("No response from ChatGPT")
    }
}

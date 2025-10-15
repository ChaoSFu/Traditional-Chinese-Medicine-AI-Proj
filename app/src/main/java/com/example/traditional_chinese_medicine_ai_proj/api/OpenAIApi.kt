package com.example.traditional_chinese_medicine_ai_proj.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * OpenAI ChatGPT API 接口
 */
interface OpenAIApi {

    @POST("v1/chat/completions")
    suspend fun chat(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): ChatResponse
}

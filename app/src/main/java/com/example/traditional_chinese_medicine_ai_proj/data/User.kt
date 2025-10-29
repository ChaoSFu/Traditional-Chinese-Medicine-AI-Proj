package com.example.traditional_chinese_medicine_ai_proj.data

/**
 * 用户数据模型
 */
data class User(
    val id: Int,
    val name: String,
    val avatar: String = "",
    val phone: String,
    val gender: String = "",
    val age: Int = 0,
    val constitution: String = "",
    val points: Int = 0,
    val level: String = "",
    val registeredDate: String = "",
    val bio: String = "",
    val health_goals: List<String> = emptyList()
)

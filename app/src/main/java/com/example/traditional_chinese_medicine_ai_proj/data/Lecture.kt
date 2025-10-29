package com.example.traditional_chinese_medicine_ai_proj.data

/**
 * 讲座与活动数据模型
 */
data class Lecture(
    val id: Int,
    val title: String,
    val type: String, // lecture(讲座), workshop(工作坊), health_talk(健康讲座), activity(活动)
    val speaker: String,
    val speakerTitle: String,
    val date: String,
    val time: String,
    val location: String,
    val description: String,
    val topics: List<String>,
    val capacity: Int,
    val registered: Int,
    val status: String, // upcoming(即将开始), ongoing(进行中), completed(已结束), full(已满员)
    val imageUrl: String = "",
    val requiresRegistration: Boolean = true
)

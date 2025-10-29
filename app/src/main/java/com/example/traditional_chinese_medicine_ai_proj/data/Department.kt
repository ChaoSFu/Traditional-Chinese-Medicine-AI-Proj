package com.example.traditional_chinese_medicine_ai_proj.data

/**
 * 科室详细信息数据模型
 */
data class Department(
    val id: Int,
    val name: String,
    val englishName: String,
    val description: String,
    val services: List<String>,
    val treatments: List<String>,
    val conditions: List<String>, // 适应症
    val features: List<String>, // 特色
    val doctorCount: Int,
    val icon: String = ""
)

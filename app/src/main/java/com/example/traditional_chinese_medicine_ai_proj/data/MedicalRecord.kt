package com.example.traditional_chinese_medicine_ai_proj.data

/**
 * 就诊记录数据模型
 */
data class MedicalRecord(
    val id: Int,
    val date: String,
    val time: String = "",              // 预约时间
    val doctor: String,
    val doctorId: Int,
    val dept: String,
    val status: String = "completed",   // 状态：pending(待诊断), completed(已完成)
    val diagnosis: String = "",
    val symptoms: String = "",
    val treatment: String = "",
    val prescription: String = "",
    val progress: String = "",
    val nextVisit: String = "",
    val notes: String = ""
)

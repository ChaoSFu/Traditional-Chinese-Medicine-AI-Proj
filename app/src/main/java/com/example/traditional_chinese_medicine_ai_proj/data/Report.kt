package com.example.traditional_chinese_medicine_ai_proj.data

/**
 * 病情上报记录
 */
data class Report(
    val id: Int,
    val date: String,              // 上报日期
    val time: String,              // 上报时间
    val symptoms: List<String>,    // 症状列表
    val description: String,       // 详细描述
    val severity: String,          // 严重程度: "轻度", "中度", "重度"
    val images: List<String> = emptyList(),  // 图片路径列表
    val status: String,            // 状态: "已上报", "医生已查看", "已回复"
    val doctorReply: String = "",  // 医生回复
    val replyTime: String = ""     // 回复时间
)

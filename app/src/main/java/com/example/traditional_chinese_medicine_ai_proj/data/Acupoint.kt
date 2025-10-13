package com.example.traditional_chinese_medicine_ai_proj.data

/**
 * 穴位数据模型
 * Data model for acupuncture points
 */
data class Acupoint(
    val id: String,                    // 穴位代码，如 "LI4"
    val nameCn: String,                // 中文名称，如 "合谷"
    val nameEn: String = "",           // 英文名称（可选）
    val meridian: String,              // 所属经络
    val location: String,              // 定位描述
    val functions: List<String>,       // 主治功效
    val x: Float = 0f,                // 当前检测到的X坐标（归一化）
    val y: Float = 0f                 // 当前检测到的Y坐标（归一化）
)

/**
 * 穴位类型枚举
 */
enum class AcupointType {
    LI4,    // 合谷
    PC8     // 劳宫
}

package com.example.traditional_chinese_medicine_ai_proj.data

/**
 * 预约挂号数据模型
 */
data class AppointmentSchedule(
    val doctor_id: Int,
    val doctor_name: String,
    val week: List<DaySlot>
)

data class DaySlot(
    val date: String,
    val dayOfWeek: String,
    val slots: List<String>
)

package com.example.traditional_chinese_medicine_ai_proj.data

/**
 * 医师数据模型
 */
data class Doctor(
    val id: Int,
    val name: String,
    val avatar: String = "",
    val dept: String,
    val title: String,
    val specialty: String,
    val years: Int,
    val rating: Double,
    val education: String = "",
    val intro: String = "",
    val schedule: List<DoctorSchedule> = emptyList()
)

/**
 * 医师排班表
 */
data class DoctorSchedule(
    val date: String,           // 日期，如 "2025-10-28"
    val dayOfWeek: String,      // 星期几，如 "周一"
    val timeSlots: List<TimeSlot>
)

/**
 * 时间段（小时级别）
 */
data class TimeSlot(
    val time: String,           // 具体时间点，如 "08:00"、"09:00"
    val available: Boolean,     // 是否可预约
    val isBooked: Boolean = false  // 是否已被预约
)

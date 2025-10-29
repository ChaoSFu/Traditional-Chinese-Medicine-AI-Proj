package com.example.traditional_chinese_medicine_ai_proj.data

/**
 * 今日任务数据模型
 */
data class TodayTaskList(
    val date: String,
    val tasks: List<TodayTask>
)

data class TodayTask(
    val id: Int,
    val type: String, // medicine, massage, appointment, exercise
    val title: String,
    val time: String,
    val duration: String = "",
    var completed: Boolean = false,
    val points: Int = 0,
    val description: String = ""
)

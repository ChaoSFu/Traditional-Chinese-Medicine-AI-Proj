package com.example.traditional_chinese_medicine_ai_proj.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * 任务时间辅助工具
 * 用于计算倒计时、判断是否过期
 */
object TaskTimeHelper {

    /**
     * 任务状态
     */
    enum class TaskStatus {
        UPCOMING,    // 即将开始（30分钟内）
        FUTURE,      // 未来的任务
        OVERDUE      // 已过期
    }

    /**
     * 任务时间信息
     */
    data class TaskTimeInfo(
        val status: TaskStatus,
        val displayText: String,
        val isOverdue: Boolean
    )

    /**
     * 获取任务的时间信息
     * @param taskTime 任务时间，格式："08:00"
     * @return 任务时间信息
     */
    fun getTaskTimeInfo(taskTime: String): TaskTimeInfo {
        try {
            val currentTime = Calendar.getInstance()
            val taskCalendar = Calendar.getInstance().apply {
                val parts = taskTime.split(":")
                set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                set(Calendar.MINUTE, parts[1].toInt())
                set(Calendar.SECOND, 0)
            }

            val diffMinutes = ((taskCalendar.timeInMillis - currentTime.timeInMillis) / (1000 * 60)).toInt()

            return when {
                // 已过期（任务时间已过）
                diffMinutes < 0 -> {
                    val overdueMins = -diffMinutes
                    val displayText = when {
                        overdueMins < 60 -> "已过期 ${overdueMins}分钟"
                        overdueMins < 1440 -> "已过期 ${overdueMins / 60}小时"
                        else -> "已过期"
                    }
                    TaskTimeInfo(TaskStatus.OVERDUE, displayText, true)
                }
                // 即将开始（30分钟内）
                diffMinutes in 0..30 -> {
                    val displayText = when {
                        diffMinutes == 0 -> "现在开始"
                        diffMinutes <= 5 -> "还有 ${diffMinutes}分钟"
                        else -> "还有 ${diffMinutes}分钟"
                    }
                    TaskTimeInfo(TaskStatus.UPCOMING, displayText, false)
                }
                // 未来的任务（30分钟后）
                else -> {
                    val hours = diffMinutes / 60
                    val mins = diffMinutes % 60
                    val displayText = when {
                        hours == 0 -> "还有 ${mins}分钟"
                        mins == 0 -> "还有 ${hours}小时"
                        else -> "还有 ${hours}小时${mins}分钟"
                    }
                    TaskTimeInfo(TaskStatus.FUTURE, displayText, false)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return TaskTimeInfo(TaskStatus.FUTURE, "", false)
        }
    }

    /**
     * 判断任务是否即将开始（15分钟内）
     */
    fun isTaskUpcoming(taskTime: String, withinMinutes: Int = 15): Boolean {
        try {
            val currentTime = Calendar.getInstance()
            val taskCalendar = Calendar.getInstance().apply {
                val parts = taskTime.split(":")
                set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                set(Calendar.MINUTE, parts[1].toInt())
                set(Calendar.SECOND, 0)
            }

            val diffMinutes = ((taskCalendar.timeInMillis - currentTime.timeInMillis) / (1000 * 60)).toInt()
            return diffMinutes in 0..withinMinutes
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * 判断任务是否已过期
     */
    fun isTaskOverdue(taskTime: String): Boolean {
        try {
            val currentTime = Calendar.getInstance()
            val taskCalendar = Calendar.getInstance().apply {
                val parts = taskTime.split(":")
                set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                set(Calendar.MINUTE, parts[1].toInt())
                set(Calendar.SECOND, 0)
            }

            return taskCalendar.timeInMillis < currentTime.timeInMillis
        } catch (e: Exception) {
            return false
        }
    }
}

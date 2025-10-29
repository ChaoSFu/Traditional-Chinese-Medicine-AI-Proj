package com.example.traditional_chinese_medicine_ai_proj.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.traditional_chinese_medicine_ai_proj.MainActivity
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.data.TodayTask
import java.util.*

/**
 * 任务通知管理器
 * 用于设置任务提醒通知
 */
object TaskNotificationManager {

    const val CHANNEL_ID = "task_reminder_channel"
    private const val CHANNEL_NAME = "任务提醒"
    private const val CHANNEL_DESC = "中医养生任务提醒通知"

    /**
     * 创建通知渠道（Android 8.0+）
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 为任务设置提醒
     * @param context 上下文
     * @param task 任务
     * @param minutesBefore 提前几分钟提醒（默认15分钟）
     */
    fun scheduleTaskReminder(context: Context, task: TodayTask, minutesBefore: Int = 15) {
        try {
            val taskCalendar = Calendar.getInstance().apply {
                val parts = task.time.split(":")
                set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                set(Calendar.MINUTE, parts[1].toInt())
                set(Calendar.SECOND, 0)
            }

            // 提醒时间 = 任务时间 - minutesBefore
            val reminderCalendar = taskCalendar.clone() as Calendar
            reminderCalendar.add(Calendar.MINUTE, -minutesBefore)

            val currentTime = Calendar.getInstance()

            // 只为未来的任务设置提醒
            if (reminderCalendar.timeInMillis > currentTime.timeInMillis) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, TaskReminderReceiver::class.java).apply {
                    putExtra("task_title", task.title)
                    putExtra("task_time", task.time)
                    putExtra("task_points", task.points)
                    putExtra("task_desc", task.description)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    task.hashCode(), // 使用任务的hashCode作为唯一ID
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // 设置精确闹钟
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderCalendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        reminderCalendar.timeInMillis,
                        pendingIntent
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 取消任务提醒
     */
    fun cancelTaskReminder(context: Context, task: TodayTask) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TaskReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 为所有未完成的任务设置提醒
     */
    fun scheduleAllTaskReminders(context: Context, tasks: List<TodayTask>) {
        createNotificationChannel(context)
        tasks.filter { !it.completed }.forEach { task ->
            scheduleTaskReminder(context, task)
        }
    }
}

/**
 * 任务提醒广播接收器
 */
class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskTitle = intent.getStringExtra("task_title") ?: "任务提醒"
        val taskTime = intent.getStringExtra("task_time") ?: ""
        val taskPoints = intent.getIntExtra("task_points", 0)
        val taskDesc = intent.getStringExtra("task_desc") ?: ""

        showNotification(context, taskTitle, taskTime, taskPoints, taskDesc)
    }

    private fun showNotification(
        context: Context,
        title: String,
        time: String,
        points: Int,
        description: String
    ) {
        // 点击通知打开App
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_tasks", true) // 打开任务页面
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 构建通知内容
        val contentText = buildString {
            append("$time 开始")
            if (points > 0) {
                append(" • 完成可获得 +$points 积分")
            }
        }

        val bigText = buildString {
            append(contentText)
            if (description.isNotEmpty()) {
                append("\n$description")
            }
        }

        val notification = NotificationCompat.Builder(context, TaskNotificationManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_task)
            .setContentTitle("🌿 $title")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}

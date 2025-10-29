package com.example.traditional_chinese_medicine_ai_proj.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.data.TodayTask
import com.example.traditional_chinese_medicine_ai_proj.utils.TaskTimeHelper

class TodayTaskAdapter(
    private val tasks: List<TodayTask>,
    private val onTaskCompleted: (TodayTask, Int) -> Unit
) : RecyclerView.Adapter<TodayTaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivTaskIcon: ImageView = view.findViewById(R.id.ivTaskIcon)
        val tvTaskTitle: TextView = view.findViewById(R.id.tvTaskTitle)
        val tvTaskTime: TextView = view.findViewById(R.id.tvTaskTime)
        val tvCountdown: TextView = view.findViewById(R.id.tvCountdown)
        val tvTaskDesc: TextView = view.findViewById(R.id.tvTaskDesc)
        val cbCompleted: CheckBox = view.findViewById(R.id.cbCompleted)
        val tvPoints: TextView = view.findViewById(R.id.tvPoints)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_today_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        // 设置图标
        val iconRes = when (task.type) {
            "medicine" -> R.drawable.ic_medicine
            "massage" -> R.drawable.ic_massage
            "appointment" -> R.drawable.ic_calendar
            "exercise" -> R.drawable.ic_exercise
            else -> R.drawable.ic_task
        }
        holder.ivTaskIcon.setImageResource(iconRes)

        holder.tvTaskTitle.text = task.title
        holder.tvTaskTime.text = task.time

        if (task.description.isNotEmpty()) {
            holder.tvTaskDesc.text = task.description
            holder.tvTaskDesc.visibility = View.VISIBLE
        } else {
            holder.tvTaskDesc.visibility = View.GONE
        }

        if (task.points > 0) {
            holder.tvPoints.text = "+${task.points}积分"
            holder.tvPoints.visibility = View.VISIBLE
        } else {
            holder.tvPoints.visibility = View.GONE
        }

        holder.cbCompleted.isChecked = task.completed

        // 如果任务已完成，添加删除线效果
        if (task.completed) {
            holder.tvTaskTitle.paintFlags = holder.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            // 已完成任务不显示倒计时
            holder.tvCountdown.visibility = View.GONE
        } else {
            holder.tvTaskTitle.paintFlags = holder.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

            // 未完成任务显示倒计时或过期状态
            val timeInfo = TaskTimeHelper.getTaskTimeInfo(task.time)

            when {
                // 已过期
                timeInfo.isOverdue -> {
                    holder.tvCountdown.visibility = View.VISIBLE
                    holder.tvCountdown.text = timeInfo.displayText
                    holder.tvCountdown.setBackgroundResource(R.drawable.bg_countdown_overdue)
                }
                // 即将开始（30分钟内）
                timeInfo.status == TaskTimeHelper.TaskStatus.UPCOMING -> {
                    holder.tvCountdown.visibility = View.VISIBLE
                    holder.tvCountdown.text = timeInfo.displayText
                    holder.tvCountdown.setBackgroundResource(R.drawable.bg_countdown_upcoming)
                }
                // 未来任务（30分钟后）
                timeInfo.status == TaskTimeHelper.TaskStatus.FUTURE -> {
                    holder.tvCountdown.visibility = View.VISIBLE
                    holder.tvCountdown.text = timeInfo.displayText
                    holder.tvCountdown.setBackgroundResource(R.drawable.bg_countdown)
                }
            }
        }

        // 支持切换任务完成状态（可以完成也可以取消）
        holder.cbCompleted.setOnClickListener {
            onTaskCompleted(task, position)
        }

        holder.itemView.setOnClickListener {
            holder.cbCompleted.isChecked = !task.completed
            onTaskCompleted(task, position)
        }
    }

    override fun getItemCount() = tasks.size
}

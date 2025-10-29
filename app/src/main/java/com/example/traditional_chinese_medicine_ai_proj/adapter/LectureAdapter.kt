package com.example.traditional_chinese_medicine_ai_proj.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.data.Lecture

class LectureAdapter(
    private val lectures: List<Lecture>,
    private val onLectureClick: (Lecture) -> Unit
) : RecyclerView.Adapter<LectureAdapter.LectureViewHolder>() {

    inner class LectureViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvType: TextView = view.findViewById(R.id.tvType)
        val tvSpeaker: TextView = view.findViewById(R.id.tvSpeaker)
        val tvDateTime: TextView = view.findViewById(R.id.tvDateTime)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvCapacity: TextView = view.findViewById(R.id.tvCapacity)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvTopics: TextView = view.findViewById(R.id.tvTopics)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LectureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lecture, parent, false)
        return LectureViewHolder(view)
    }

    override fun onBindViewHolder(holder: LectureViewHolder, position: Int) {
        val lecture = lectures[position]

        holder.tvTitle.text = lecture.title
        holder.tvType.text = getTypeText(lecture.type)
        holder.tvSpeaker.text = "${lecture.speaker} ${lecture.speakerTitle}"
        holder.tvDateTime.text = "${lecture.date} ${lecture.time}"
        holder.tvLocation.text = lecture.location

        // 显示容量信息
        if (lecture.requiresRegistration) {
            holder.tvCapacity.text = "名额：${lecture.registered}/${lecture.capacity}"
            holder.tvCapacity.visibility = View.VISIBLE
        } else {
            holder.tvCapacity.visibility = View.GONE
        }

        // 设置状态
        holder.tvStatus.text = getStatusText(lecture.status)
        holder.tvStatus.setTextColor(
            when (lecture.status) {
                "upcoming" -> holder.itemView.context.getColor(android.R.color.holo_green_dark)
                "full" -> holder.itemView.context.getColor(android.R.color.holo_red_dark)
                "completed" -> holder.itemView.context.getColor(android.R.color.darker_gray)
                else -> holder.itemView.context.getColor(android.R.color.holo_orange_dark)
            }
        )

        // 显示主题
        holder.tvTopics.text = "主题：${lecture.topics.joinToString("、")}"

        holder.itemView.setOnClickListener {
            onLectureClick(lecture)
        }
    }

    override fun getItemCount() = lectures.size

    private fun getTypeText(type: String): String {
        return when (type) {
            "lecture" -> "[讲座]"
            "workshop" -> "[工作坊]"
            "health_talk" -> "[健康讲座]"
            "activity" -> "[活动]"
            else -> ""
        }
    }

    private fun getStatusText(status: String): String {
        return when (status) {
            "upcoming" -> "即将开始"
            "ongoing" -> "进行中"
            "completed" -> "已结束"
            "full" -> "已满员"
            else -> ""
        }
    }
}

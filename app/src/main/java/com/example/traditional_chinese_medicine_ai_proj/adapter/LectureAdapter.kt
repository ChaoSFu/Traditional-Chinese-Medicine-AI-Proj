package com.example.traditional_chinese_medicine_ai_proj.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.data.Lecture

class LectureAdapter(
    private val lectures: List<Lecture>,
    private val onLectureClick: (Lecture) -> Unit
) : RecyclerView.Adapter<LectureAdapter.LectureViewHolder>() {

    inner class LectureViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivLectureCover: ImageView = view.findViewById(R.id.ivLectureCover)
        val tvLectureTitle: TextView = view.findViewById(R.id.tvLectureTitle)
        val tvLectureSpeaker: TextView = view.findViewById(R.id.tvLectureSpeaker)
        val tvLectureStatus: TextView = view.findViewById(R.id.tvLectureStatus)
        val tvLectureDateTime: TextView = view.findViewById(R.id.tvLectureDateTime)
        val tvParticipants: TextView = view.findViewById(R.id.tvParticipants)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LectureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lecture, parent, false)
        return LectureViewHolder(view)
    }

    override fun onBindViewHolder(holder: LectureViewHolder, position: Int) {
        val lecture = lectures[position]

        // 加载封面图片
        if (lecture.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(lecture.imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.bg_points_card)
                .error(R.drawable.bg_points_card)
                .into(holder.ivLectureCover)
        } else {
            holder.ivLectureCover.setImageResource(R.drawable.bg_points_card)
        }

        holder.tvLectureTitle.text = lecture.title
        holder.tvLectureSpeaker.text = lecture.speaker
        holder.tvLectureDateTime.text = "${lecture.date} ${lecture.time}"
        holder.tvParticipants.text = "${lecture.registered}人已报名"

        // 根据状态设置样式
        when (lecture.status) {
            "upcoming" -> {
                holder.tvLectureStatus.text = "即将开始"
                holder.tvLectureStatus.setTextColor(holder.itemView.context.getColor(R.color.tcm_primary))
                holder.tvLectureStatus.setBackgroundResource(R.drawable.bg_tag_category)
            }
            "ongoing" -> {
                holder.tvLectureStatus.text = "进行中"
                holder.tvLectureStatus.setTextColor(holder.itemView.context.getColor(R.color.tcm_accent))
                holder.tvLectureStatus.setBackgroundResource(R.drawable.bg_tag_default)
            }
            "completed" -> {
                holder.tvLectureStatus.text = "已结束"
                holder.tvLectureStatus.setTextColor(holder.itemView.context.getColor(android.R.color.darker_gray))
                holder.tvLectureStatus.setBackgroundResource(R.drawable.bg_tag_default)
            }
            "full" -> {
                holder.tvLectureStatus.text = "已满员"
                holder.tvLectureStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
                holder.tvLectureStatus.setBackgroundResource(R.drawable.bg_tag_default)
            }
        }

        holder.itemView.setOnClickListener {
            onLectureClick(lecture)
        }
    }

    override fun getItemCount() = lectures.size
}

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

class LectureHorizontalAdapter(
    private val lectures: List<Lecture>,
    private val onItemClick: (Lecture) -> Unit
) : RecyclerView.Adapter<LectureHorizontalAdapter.LectureViewHolder>() {

    inner class LectureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivLectureCover: ImageView = itemView.findViewById(R.id.ivLectureCover)
        val tvLectureTitle: TextView = itemView.findViewById(R.id.tvLectureTitle)
        val tvLectureSpeaker: TextView = itemView.findViewById(R.id.tvLectureSpeaker)
        val tvLectureStatus: TextView = itemView.findViewById(R.id.tvLectureStatus)
        val tvLectureDateTime: TextView = itemView.findViewById(R.id.tvLectureDateTime)
        val tvParticipants: TextView = itemView.findViewById(R.id.tvParticipants)

        fun bind(lecture: Lecture) {
            // 加载封面图片
            if (lecture.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(lecture.imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.bg_points_card)
                    .error(R.drawable.bg_points_card)
                    .into(ivLectureCover)
            } else {
                ivLectureCover.setImageResource(R.drawable.bg_points_card)
            }

            tvLectureTitle.text = lecture.title
            tvLectureSpeaker.text = lecture.speaker
            tvLectureDateTime.text = "${lecture.date} ${lecture.time}"
            tvParticipants.text = "${lecture.registered}人已报名"

            // 根据状态设置样式
            when (lecture.status) {
                "upcoming" -> {
                    tvLectureStatus.text = "即将开始"
                    tvLectureStatus.setTextColor(itemView.context.getColor(R.color.tcm_primary))
                    tvLectureStatus.setBackgroundResource(R.drawable.bg_tag_category)
                }
                "ongoing" -> {
                    tvLectureStatus.text = "进行中"
                    tvLectureStatus.setTextColor(itemView.context.getColor(R.color.tcm_accent))
                    tvLectureStatus.setBackgroundResource(R.drawable.bg_tag_default)
                }
                "completed" -> {
                    tvLectureStatus.text = "已结束"
                    tvLectureStatus.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                    tvLectureStatus.setBackgroundResource(R.drawable.bg_tag_default)
                }
            }

            itemView.setOnClickListener {
                onItemClick(lecture)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LectureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lecture_horizontal, parent, false)
        return LectureViewHolder(view)
    }

    override fun onBindViewHolder(holder: LectureViewHolder, position: Int) {
        holder.bind(lectures[position])
    }

    override fun getItemCount(): Int = lectures.size
}

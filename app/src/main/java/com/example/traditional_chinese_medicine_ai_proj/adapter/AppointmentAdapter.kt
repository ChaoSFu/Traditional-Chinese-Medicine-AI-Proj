package com.example.traditional_chinese_medicine_ai_proj.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.data.AppointmentSchedule
import com.example.traditional_chinese_medicine_ai_proj.data.DaySlot
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class AppointmentAdapter(
    private val schedules: List<AppointmentSchedule>,
    private val onSlotClick: (AppointmentSchedule, DaySlot, String) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    inner class AppointmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDoctorName: TextView = view.findViewById(R.id.tvDoctorName)
        val containerDays: ViewGroup = view.findViewById(R.id.containerDays)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val schedule = schedules[position]

        holder.tvDoctorName.text = "${schedule.doctor_name} 医师"
        holder.containerDays.removeAllViews()

        // 为每一天创建一个卡片
        schedule.week.forEach { daySlot ->
            val dayView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.item_day_slot, holder.containerDays, false)

            val tvDate = dayView.findViewById<TextView>(R.id.tvDate)
            val chipGroup = dayView.findViewById<ChipGroup>(R.id.chipGroupSlots)

            tvDate.text = "${daySlot.date} (${daySlot.dayOfWeek})"

            // 添加时间段Chip
            daySlot.slots.forEach { time ->
                val chip = Chip(holder.itemView.context).apply {
                    text = time
                    isClickable = true
                    isCheckable = false
                    setOnClickListener {
                        onSlotClick(schedule, daySlot, time)
                    }
                }
                chipGroup.addView(chip)
            }

            holder.containerDays.addView(dayView)
        }
    }

    override fun getItemCount() = schedules.size
}

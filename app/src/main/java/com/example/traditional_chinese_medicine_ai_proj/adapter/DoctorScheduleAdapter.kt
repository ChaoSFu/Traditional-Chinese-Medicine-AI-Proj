package com.example.traditional_chinese_medicine_ai_proj.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.data.DoctorSchedule
import com.example.traditional_chinese_medicine_ai_proj.data.TimeSlot

/**
 * 医师排班表Adapter
 */
class DoctorScheduleAdapter(
    private val onTimeSlotClick: (date: String, dayOfWeek: String, timeSlot: TimeSlot) -> Unit
) : RecyclerView.Adapter<DoctorScheduleAdapter.ScheduleViewHolder>() {

    private val schedules = mutableListOf<DoctorSchedule>()

    fun submitSchedule(newSchedules: List<DoctorSchedule>) {
        schedules.clear()
        schedules.addAll(newSchedules)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = schedules[position]
        holder.bind(schedule)
    }

    override fun getItemCount() = schedules.size

    inner class ScheduleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvDate: TextView = view.findViewById(R.id.tvDate)
        private val tvDayOfWeek: TextView = view.findViewById(R.id.tvDayOfWeek)
        private val recyclerTimeSlots: RecyclerView = view.findViewById(R.id.recyclerTimeSlots)

        fun bind(schedule: DoctorSchedule) {
            tvDate.text = schedule.date
            tvDayOfWeek.text = schedule.dayOfWeek

            // 设置网格布局
            recyclerTimeSlots.layoutManager = GridLayoutManager(itemView.context, 4)

            // 设置时间段适配器
            val timeSlotAdapter = TimeSlotAdapter(schedule.date, schedule.dayOfWeek, schedule.timeSlots, onTimeSlotClick)
            recyclerTimeSlots.adapter = timeSlotAdapter
        }
    }
}

/**
 * 时间段网格Adapter
 */
class TimeSlotAdapter(
    private val date: String,
    private val dayOfWeek: String,
    private val timeSlots: List<TimeSlot>,
    private val onTimeSlotClick: (date: String, dayOfWeek: String, timeSlot: TimeSlot) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        holder.bind(timeSlots[position])
    }

    override fun getItemCount() = timeSlots.size

    inner class TimeSlotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTime: TextView = view.findViewById(R.id.tvTime)

        fun bind(timeSlot: TimeSlot) {
            tvTime.text = timeSlot.time

            val context = itemView.context

            // 根据状态设置样式
            when {
                !timeSlot.available -> {
                    // 不可预约
                    tvTime.setBackgroundResource(R.drawable.bg_time_slot_unavailable)
                    tvTime.setTextColor(context.getColor(android.R.color.darker_gray))
                    itemView.isEnabled = false
                    itemView.alpha = 0.4f
                }
                timeSlot.isBooked -> {
                    // 已被预约
                    tvTime.setBackgroundResource(R.drawable.bg_time_slot_booked)
                    tvTime.setTextColor(context.getColor(android.R.color.white))
                    itemView.isEnabled = false
                    itemView.alpha = 0.6f
                }
                else -> {
                    // 可预约
                    tvTime.setBackgroundResource(R.drawable.bg_time_slot_available)
                    tvTime.setTextColor(context.getColor(android.R.color.holo_green_dark))
                    itemView.isEnabled = true
                    itemView.alpha = 1.0f

                    itemView.setOnClickListener {
                        onTimeSlotClick(date, dayOfWeek, timeSlot)
                    }
                }
            }
        }
    }
}

package com.example.traditional_chinese_medicine_ai_proj.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.adapter.AppointmentAdapter
import com.example.traditional_chinese_medicine_ai_proj.data.AppointmentSchedule
import com.example.traditional_chinese_medicine_ai_proj.data.DaySlot
import com.example.traditional_chinese_medicine_ai_proj.utils.MockDataLoader

/**
 * 预约挂号Fragment
 * 展示医师日程并支持预约
 */
class AppointmentFragment : Fragment() {

    private lateinit var tvNoData: TextView
    private lateinit var recyclerAppointments: RecyclerView

    private lateinit var appointmentAdapter: AppointmentAdapter
    private val schedules = mutableListOf<AppointmentSchedule>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_appointment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        loadData()
    }

    private fun initViews(view: View) {
        tvNoData = view.findViewById(R.id.tvNoData)
        recyclerAppointments = view.findViewById(R.id.recyclerAppointments)

        // 设置RecyclerView
        appointmentAdapter = AppointmentAdapter(schedules) { schedule, slot, time ->
            onSlotSelected(schedule, slot, time)
        }
        recyclerAppointments.layoutManager = LinearLayoutManager(requireContext())
        recyclerAppointments.adapter = appointmentAdapter
    }

    private fun loadData() {
        val appointmentMap = MockDataLoader.loadAppointments(requireContext())
        schedules.clear()
        schedules.addAll(appointmentMap.values)

        if (schedules.isEmpty()) {
            tvNoData.visibility = View.VISIBLE
            recyclerAppointments.visibility = View.GONE
        } else {
            tvNoData.visibility = View.GONE
            recyclerAppointments.visibility = View.VISIBLE
            appointmentAdapter.notifyDataSetChanged()
        }
    }

    private fun onSlotSelected(schedule: AppointmentSchedule, slot: DaySlot, time: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("预约确认")
            .setMessage(
                "医师：${schedule.doctor_name}\n" +
                        "日期：${slot.date} (${slot.dayOfWeek})\n" +
                        "时间：$time\n\n" +
                        "确认预约？"
            )
            .setPositiveButton("确认") { _, _ ->
                Toast.makeText(
                    requireContext(),
                    "预约成功！请准时就诊",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
}

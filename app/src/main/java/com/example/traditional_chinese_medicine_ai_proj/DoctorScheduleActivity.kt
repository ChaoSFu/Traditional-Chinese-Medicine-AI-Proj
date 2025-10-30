package com.example.traditional_chinese_medicine_ai_proj

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.adapter.DoctorScheduleAdapter
import com.example.traditional_chinese_medicine_ai_proj.data.Doctor
import com.example.traditional_chinese_medicine_ai_proj.data.DoctorSchedule
import com.example.traditional_chinese_medicine_ai_proj.data.TimeSlot
import com.example.traditional_chinese_medicine_ai_proj.utils.MockDataLoader
import com.example.traditional_chinese_medicine_ai_proj.utils.AppointmentManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * 医师排班表Activity
 * 显示医师值班表，支持预约时间段选择
 */
class DoctorScheduleActivity : AppCompatActivity() {

    private lateinit var tvDoctorName: TextView
    private lateinit var tvDoctorTitle: TextView
    private lateinit var tvDoctorDept: TextView
    private lateinit var recyclerSchedule: RecyclerView
    private lateinit var btnBack: Button

    private lateinit var scheduleAdapter: DoctorScheduleAdapter
    private var doctor: Doctor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_schedule)

        initViews()
        loadData()
        setupListeners()
    }

    private fun initViews() {
        tvDoctorName = findViewById(R.id.tvDoctorName)
        tvDoctorTitle = findViewById(R.id.tvDoctorTitle)
        tvDoctorDept = findViewById(R.id.tvDoctorDept)
        recyclerSchedule = findViewById(R.id.recyclerSchedule)
        btnBack = findViewById(R.id.btnBack)

        // 设置RecyclerView
        scheduleAdapter = DoctorScheduleAdapter { date, dayOfWeek, timeSlot ->
            onTimeSlotSelected(date, dayOfWeek, timeSlot)
        }
        recyclerSchedule.layoutManager = LinearLayoutManager(this)
        recyclerSchedule.adapter = scheduleAdapter
    }

    private fun loadData() {
        val doctorId = intent.getIntExtra("DOCTOR_ID", -1)
        if (doctorId == -1) {
            Toast.makeText(this, "医师信息错误", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        doctor = MockDataLoader.getDoctorById(this, doctorId)
        if (doctor == null) {
            Toast.makeText(this, "医师信息不存在", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 显示医师信息
        tvDoctorName.text = doctor!!.name
        tvDoctorTitle.text = doctor!!.title
        tvDoctorDept.text = doctor!!.dept

        // 加载排班表并过滤已过期的日期和时段
        val filteredSchedule = filterScheduleByCurrentTime(doctor!!.schedule)
        scheduleAdapter.submitSchedule(filteredSchedule)
    }

    /**
     * 根据当前时间过滤排班表
     * 1. 移除已经过去的日期
     * 2. 对当天的时段，标记已过去的时段为不可用
     */
    private fun filterScheduleByCurrentTime(schedules: List<DoctorSchedule>): List<DoctorSchedule> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val currentDate = dateFormat.format(calendar.time)
        val currentTime = timeFormat.format(calendar.time)

        // 解析当前日期为 Date 对象，用于日期比较
        val currentDateObj = dateFormat.parse(currentDate)

        return schedules.mapNotNull { schedule ->
            // 解析排班日期
            val scheduleDate = try {
                dateFormat.parse(schedule.date)
            } catch (e: Exception) {
                null
            }

            when {
                scheduleDate == null -> null // 日期格式错误，跳过
                scheduleDate.before(currentDateObj) -> null // 已过去的日期，不显示
                schedule.date == currentDate -> {
                    // 当天的排班，需要过滤已过去的时段
                    val updatedTimeSlots = schedule.timeSlots.map { timeSlot ->
                        if (timeSlot.time < currentTime) {
                            // 时段已过去，设置为不可用
                            timeSlot.copy(available = false)
                        } else {
                            timeSlot
                        }
                    }
                    schedule.copy(timeSlots = updatedTimeSlots)
                }
                else -> schedule // 未来的日期，保持不变
            }
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun onTimeSlotSelected(date: String, dayOfWeek: String, timeSlot: TimeSlot) {
        if (!timeSlot.available) {
            Toast.makeText(this, "该时段不可预约", Toast.LENGTH_SHORT).show()
            return
        }

        if (timeSlot.isBooked) {
            Toast.makeText(this, "该时段已被预约", Toast.LENGTH_SHORT).show()
            return
        }

        // 显示预约确认对话框
        AlertDialog.Builder(this)
            .setTitle("确认预约")
            .setMessage(
                "医师：${doctor?.name}\n" +
                "科室：${doctor?.dept}\n" +
                "日期：$date ($dayOfWeek)\n" +
                "时间：${timeSlot.time}\n\n" +
                "确认预约吗？"
            )
            .setPositiveButton("确认") { _, _ ->
                confirmBooking(date, dayOfWeek, timeSlot)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun confirmBooking(date: String, dayOfWeek: String, timeSlot: TimeSlot) {
        doctor?.let { doc ->
            // 创建预约记录（待诊断状态）
            val appointment = AppointmentManager.createAppointment(
                context = this,
                date = date,
                time = timeSlot.time,
                doctorId = doc.id,
                doctorName = doc.name,
                dept = doc.dept
            )

            // 显示温暖的提示弹窗
            com.example.traditional_chinese_medicine_ai_proj.ui.dialog.TcmTipDialog.show(
                this,
                com.example.traditional_chinese_medicine_ai_proj.utils.TcmTipHelper.Category.APPOINTMENT
            )

            // 延迟返回，让用户看到提示
            window.decorView.postDelayed({
                finish()
            }, 2000)
        }
    }
}

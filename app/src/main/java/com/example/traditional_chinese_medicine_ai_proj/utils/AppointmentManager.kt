package com.example.traditional_chinese_medicine_ai_proj.utils

import android.content.Context
import com.example.traditional_chinese_medicine_ai_proj.data.MedicalRecord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 预约记录管理工具
 * 使用SharedPreferences存储预约记录
 */
object AppointmentManager {
    private const val PREF_NAME = "appointments"
    private const val KEY_APPOINTMENTS = "appointment_list"
    private const val KEY_NEXT_ID = "next_appointment_id"

    private val gson = Gson()

    /**
     * 创建新的预约记录
     */
    fun createAppointment(
        context: Context,
        date: String,
        time: String,
        doctorId: Int,
        doctorName: String,
        dept: String
    ): MedicalRecord {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // 生成新ID
        val nextId = prefs.getInt(KEY_NEXT_ID, 1000)

        // 创建待诊断记录
        val appointment = MedicalRecord(
            id = nextId,
            date = date,
            time = time,
            doctor = doctorName,
            doctorId = doctorId,
            dept = dept,
            status = "pending",
            diagnosis = "待诊断",
            symptoms = "",
            treatment = "",
            prescription = "",
            progress = "",
            nextVisit = "",
            notes = "预约时间：$date $time"
        )

        // 保存记录
        val appointments = getAppointments(context).toMutableList()
        appointments.add(0, appointment) // 添加到列表开头

        val json = gson.toJson(appointments)
        prefs.edit()
            .putString(KEY_APPOINTMENTS, json)
            .putInt(KEY_NEXT_ID, nextId + 1)
            .apply()

        return appointment
    }

    /**
     * 获取所有预约记录
     */
    fun getAppointments(context: Context): List<MedicalRecord> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_APPOINTMENTS, null) ?: return emptyList()

        return try {
            val type = object : TypeToken<List<MedicalRecord>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 获取待诊断的预约记录
     */
    fun getPendingAppointments(context: Context): List<MedicalRecord> {
        return getAppointments(context).filter { it.status == "pending" }
    }

    /**
     * 删除预约记录
     */
    fun deleteAppointment(context: Context, appointmentId: Int) {
        val appointments = getAppointments(context).toMutableList()
        appointments.removeAll { it.id == appointmentId }

        val json = gson.toJson(appointments)
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_APPOINTMENTS, json)
            .apply()
    }

    /**
     * 更新预约记录状态
     */
    fun updateAppointmentStatus(context: Context, appointmentId: Int, newStatus: String) {
        val appointments = getAppointments(context).toMutableList()
        val index = appointments.indexOfFirst { it.id == appointmentId }

        if (index != -1) {
            appointments[index] = appointments[index].copy(status = newStatus)

            val json = gson.toJson(appointments)
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_APPOINTMENTS, json)
                .apply()
        }
    }

    /**
     * 清空所有预约记录（用于测试）
     */
    fun clearAllAppointments(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}

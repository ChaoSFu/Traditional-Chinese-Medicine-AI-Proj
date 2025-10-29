package com.example.traditional_chinese_medicine_ai_proj.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.data.MedicalRecord
import java.text.SimpleDateFormat
import java.util.*

class RecordAdapter(
    private val records: List<MedicalRecord>,
    private val onCancelAppointment: ((MedicalRecord, Boolean) -> Unit)? = null
) : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {

    companion object {
        private const val CANCEL_THRESHOLD_HOURS = 3 // 取消预约的最小提前时间（小时）
    }

    inner class RecordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvDoctor: TextView = view.findViewById(R.id.tvDoctor)
        val tvDept: TextView = view.findViewById(R.id.tvDept)
        val tvDiagnosis: TextView = view.findViewById(R.id.tvDiagnosis)
        val tvSymptoms: TextView = view.findViewById(R.id.tvSymptoms)
        val tvTreatment: TextView = view.findViewById(R.id.tvTreatment)
        val tvPrescription: TextView = view.findViewById(R.id.tvPrescription)
        val tvProgress: TextView = view.findViewById(R.id.tvProgress)
        val tvNextVisit: TextView = view.findViewById(R.id.tvNextVisit)
        val tvNotes: TextView = view.findViewById(R.id.tvNotes)
        val btnCancelAppointment: Button = view.findViewById(R.id.btnCancelAppointment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]

        holder.tvDate.text = record.date
        holder.tvDoctor.text = "${record.doctor} | ${record.dept}"
        holder.tvDept.text = record.dept

        // 根据状态显示不同UI
        when (record.status) {
            "pending" -> {
                // 待诊断状态
                holder.tvStatus.text = "待诊断"
                holder.tvStatus.visibility = View.VISIBLE
                holder.tvProgress.visibility = View.GONE
                holder.btnCancelAppointment.visibility = View.VISIBLE

                // 显示预约时间
                if (record.time.isNotEmpty()) {
                    holder.tvTime.text = record.time
                    holder.tvTime.visibility = View.VISIBLE
                } else {
                    holder.tvTime.visibility = View.GONE
                }

                holder.tvDiagnosis.text = "诊断：${record.diagnosis}"

                // 隐藏详细信息
                holder.tvSymptoms.visibility = if (record.symptoms.isEmpty()) View.GONE else View.VISIBLE
                holder.tvTreatment.visibility = if (record.treatment.isEmpty()) View.GONE else View.VISIBLE
                holder.tvPrescription.visibility = if (record.prescription.isEmpty()) View.GONE else View.VISIBLE

                // 检查是否可以取消预约
                val canCancel = canCancelAppointment(record)
                holder.btnCancelAppointment.isEnabled = canCancel

                // 根据是否可取消设置按钮样式
                if (canCancel) {
                    holder.btnCancelAppointment.alpha = 1.0f
                    holder.btnCancelAppointment.text = "取消预约"
                } else {
                    holder.btnCancelAppointment.alpha = 0.5f
                    holder.btnCancelAppointment.text = "不可取消"
                }

                // 设置取消按钮点击事件
                holder.btnCancelAppointment.setOnClickListener {
                    onCancelAppointment?.invoke(record, canCancel)
                }
            }
            else -> {
                // 已完成状态
                holder.tvStatus.visibility = View.GONE
                holder.tvProgress.visibility = View.VISIBLE
                holder.tvTime.visibility = View.GONE
                holder.btnCancelAppointment.visibility = View.GONE

                holder.tvProgress.text = record.progress
                when (record.progress) {
                    "进行中" -> holder.tvProgress.setTextColor(
                        holder.itemView.context.getColor(android.R.color.holo_orange_dark)
                    )
                    "已完成" -> holder.tvProgress.setTextColor(
                        holder.itemView.context.getColor(android.R.color.holo_green_dark)
                    )
                }

                holder.tvDiagnosis.text = "诊断：${record.diagnosis}"
                holder.tvSymptoms.text = "症状：${record.symptoms}"
                holder.tvTreatment.text = "治疗方案：${record.treatment}"
                holder.tvPrescription.text = "处方：${record.prescription}"

                holder.tvSymptoms.visibility = View.VISIBLE
                holder.tvTreatment.visibility = View.VISIBLE
                holder.tvPrescription.visibility = View.VISIBLE
            }
        }

        if (record.nextVisit.isNotEmpty()) {
            holder.tvNextVisit.text = "下次复诊：${record.nextVisit}"
            holder.tvNextVisit.visibility = View.VISIBLE
        } else {
            holder.tvNextVisit.visibility = View.GONE
        }

        if (record.notes.isNotEmpty()) {
            holder.tvNotes.text = "备注：${record.notes}"
            holder.tvNotes.visibility = View.VISIBLE
        } else {
            holder.tvNotes.visibility = View.GONE
        }
    }

    override fun getItemCount() = records.size

    /**
     * 检查预约是否可以取消
     * @return true 如果距离就诊时间超过3小时
     */
    private fun canCancelAppointment(record: MedicalRecord): Boolean {
        if (record.date.isEmpty() || record.time.isEmpty()) {
            return false
        }

        try {
            // 解析预约时间
            val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val appointmentDateTime = dateTimeFormat.parse("${record.date} ${record.time}")
                ?: return false

            // 获取当前时间
            val now = Calendar.getInstance()

            // 计算时间差（毫秒）
            val timeDiff = appointmentDateTime.time - now.timeInMillis

            // 转换为小时
            val hoursDiff = timeDiff / (1000 * 60 * 60)

            // 如果距离就诊时间超过3小时，返回true
            return hoursDiff >= CANCEL_THRESHOLD_HOURS
        } catch (e: Exception) {
            return false
        }
    }
}

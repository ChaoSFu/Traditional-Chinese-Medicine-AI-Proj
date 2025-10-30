package com.example.traditional_chinese_medicine_ai_proj.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.data.Report

class ReportHistoryAdapter(
    private val reports: List<Report>,
    private val onItemClick: (Report) -> Unit
) : RecyclerView.Adapter<ReportHistoryAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvReportDate: TextView = itemView.findViewById(R.id.tvReportDate)
        val tvReportStatus: TextView = itemView.findViewById(R.id.tvReportStatus)
        val tvSymptoms: TextView = itemView.findViewById(R.id.tvSymptoms)
        val tvSeverity: TextView = itemView.findViewById(R.id.tvSeverity)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val layoutDoctorReply: LinearLayout = itemView.findViewById(R.id.layoutDoctorReply)
        val btnViewDetail: TextView = itemView.findViewById(R.id.btnViewDetail)

        fun bind(report: Report) {
            tvReportDate.text = "${report.date} ${report.time}"
            tvReportStatus.text = report.status
            tvSymptoms.text = report.symptoms.joinToString("、")
            tvSeverity.text = report.severity
            tvDescription.text = report.description

            // 根据状态设置颜色
            when (report.status) {
                "已回复" -> {
                    tvReportStatus.setTextColor(itemView.context.getColor(R.color.tcm_primary))
                    tvReportStatus.setBackgroundResource(R.drawable.bg_tag_category)
                    layoutDoctorReply.visibility = View.VISIBLE
                }
                "医生已查看" -> {
                    tvReportStatus.setTextColor(itemView.context.getColor(R.color.tcm_accent))
                    tvReportStatus.setBackgroundResource(R.drawable.bg_tag_default)
                    layoutDoctorReply.visibility = View.GONE
                }
                else -> {
                    tvReportStatus.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                    tvReportStatus.setBackgroundResource(R.drawable.bg_tag_default)
                    layoutDoctorReply.visibility = View.GONE
                }
            }

            // 根据严重程度设置颜色
            when (report.severity) {
                "重度" -> tvSeverity.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                "中度" -> tvSeverity.setTextColor(itemView.context.getColor(R.color.tcm_accent))
                "轻度" -> tvSeverity.setTextColor(itemView.context.getColor(R.color.tcm_primary))
            }

            // 点击事件
            itemView.setOnClickListener {
                onItemClick(report)
            }

            btnViewDetail.setOnClickListener {
                onItemClick(report)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report_history, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }

    override fun getItemCount(): Int = reports.size
}

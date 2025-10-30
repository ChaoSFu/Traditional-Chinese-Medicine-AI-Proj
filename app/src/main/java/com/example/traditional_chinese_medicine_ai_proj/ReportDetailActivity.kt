package com.example.traditional_chinese_medicine_ai_proj

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.traditional_chinese_medicine_ai_proj.data.Report
import com.example.traditional_chinese_medicine_ai_proj.utils.MockDataLoader
import com.google.android.material.card.MaterialCardView

class ReportDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnEdit: Button
    private lateinit var tvDateTime: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvSymptoms: TextView
    private lateinit var tvSeverity: TextView
    private lateinit var tvDescription: TextView
    private lateinit var cardDoctorReply: MaterialCardView
    private lateinit var tvReplyTime: TextView
    private lateinit var tvDoctorReply: TextView

    private var currentReport: Report? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_detail)

        initViews()
        loadReportDetail()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnEdit = findViewById(R.id.btnEdit)
        tvDateTime = findViewById(R.id.tvDateTime)
        tvStatus = findViewById(R.id.tvStatus)
        tvSymptoms = findViewById(R.id.tvSymptoms)
        tvSeverity = findViewById(R.id.tvSeverity)
        tvDescription = findViewById(R.id.tvDescription)
        cardDoctorReply = findViewById(R.id.cardDoctorReply)
        tvReplyTime = findViewById(R.id.tvReplyTime)
        tvDoctorReply = findViewById(R.id.tvDoctorReply)
    }

    private fun loadReportDetail() {
        val reportId = intent.getIntExtra("REPORT_ID", -1)
        if (reportId == -1) {
            Toast.makeText(this, "报告ID无效", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentReport = MockDataLoader.getReportById(this, reportId)
        if (currentReport == null) {
            Toast.makeText(this, "未找到报告", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        displayReport(currentReport!!)
    }

    private fun displayReport(report: Report) {
        tvDateTime.text = "${report.date} ${report.time}"
        tvStatus.text = report.status
        tvSymptoms.text = report.symptoms.joinToString("、")
        tvSeverity.text = report.severity
        tvDescription.text = report.description

        // 根据状态设置颜色
        when (report.status) {
            "已回复" -> {
                tvStatus.setTextColor(getColor(R.color.tcm_primary))
                tvStatus.setBackgroundResource(R.drawable.bg_tag_category)
            }
            "医生已查看" -> {
                tvStatus.setTextColor(getColor(R.color.tcm_accent))
                tvStatus.setBackgroundResource(R.drawable.bg_tag_default)
            }
            else -> {
                tvStatus.setTextColor(getColor(android.R.color.darker_gray))
                tvStatus.setBackgroundResource(R.drawable.bg_tag_default)
            }
        }

        // 根据严重程度设置颜色
        when (report.severity) {
            "重度" -> tvSeverity.setTextColor(getColor(android.R.color.holo_red_dark))
            "中度" -> tvSeverity.setTextColor(getColor(R.color.tcm_accent))
            "轻度" -> tvSeverity.setTextColor(getColor(R.color.tcm_primary))
        }

        // 显示医生回复（如果有）
        if (report.doctorReply.isNotEmpty()) {
            cardDoctorReply.visibility = View.VISIBLE
            tvDoctorReply.text = report.doctorReply
            tvReplyTime.text = report.replyTime
        } else {
            cardDoctorReply.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnEdit.setOnClickListener {
            showEditDialog()
        }
    }

    private fun showEditDialog() {
        val report = currentReport ?: return

        // 创建编辑对话框
        val builder = AlertDialog.Builder(this)
        builder.setTitle("编辑上报记录")

        val message = """
            当前功能为演示版本。

            在完整版本中，您可以：
            • 修改症状描述
            • 更新严重程度
            • 补充额外信息

            此功能正在开发中...
        """.trimIndent()

        builder.setMessage(message)
        builder.setPositiveButton("确定") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }
}

package com.example.traditional_chinese_medicine_ai_proj

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.adapter.RecordAdapter
import com.example.traditional_chinese_medicine_ai_proj.data.MedicalRecord
import com.example.traditional_chinese_medicine_ai_proj.utils.AppointmentManager
import com.example.traditional_chinese_medicine_ai_proj.utils.MockDataLoader

/**
 * 就诊记录Activity
 * 展示历史就诊记录和治疗进度
 */
class MedicalRecordsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var recyclerRecords: RecyclerView
    private lateinit var recordAdapter: RecordAdapter
    private val records = mutableListOf<MedicalRecord>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medical_records)

        initViews()
        loadData()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        recyclerRecords = findViewById(R.id.recyclerRecords)

        // 设置RecyclerView，传入取消预约回调
        recordAdapter = RecordAdapter(records) { record, canCancel ->
            if (canCancel) {
                showCancelConfirmDialog(record)
            } else {
                showCannotCancelDialog(record)
            }
        }
        recyclerRecords.layoutManager = LinearLayoutManager(this)
        recyclerRecords.adapter = recordAdapter
    }

    private fun loadData() {
        records.clear()

        // 先加载预约记录（待诊断）
        val appointments = AppointmentManager.getAppointments(this)
        records.addAll(appointments)

        // 再加载历史就诊记录
        val historyRecords = MockDataLoader.loadRecords(this)
        records.addAll(historyRecords)

        recordAdapter.notifyDataSetChanged()
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次返回时刷新数据，确保显示最新的预约记录
        loadData()
    }

    /**
     * 显示取消预约确认对话框
     */
    private fun showCancelConfirmDialog(record: MedicalRecord) {
        AlertDialog.Builder(this)
            .setTitle("取消预约")
            .setMessage("确定要取消 ${record.date} ${record.time} 的预约吗？\n\n医生：${record.doctor}\n科室：${record.dept}")
            .setPositiveButton("确定") { _, _ ->
                cancelAppointment(record)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 显示无法取消预约的提示对话框
     */
    private fun showCannotCancelDialog(record: MedicalRecord) {
        AlertDialog.Builder(this)
            .setTitle("无法取消预约")
            .setMessage("预约时间：${record.date} ${record.time}\n\n为了不影响医生的排班和其他患者的就诊，距离就诊时间不足3小时的预约无法取消。\n\n如有特殊情况，请联系医院客服。")
            .setPositiveButton("我知道了", null)
            .show()
    }

    /**
     * 取消预约
     */
    private fun cancelAppointment(record: MedicalRecord) {
        // 从AppointmentManager中删除预约
        AppointmentManager.deleteAppointment(this, record.id)

        // 刷新列表
        loadData()

        // 显示提示
        android.widget.Toast.makeText(
            this,
            "预约已取消",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}

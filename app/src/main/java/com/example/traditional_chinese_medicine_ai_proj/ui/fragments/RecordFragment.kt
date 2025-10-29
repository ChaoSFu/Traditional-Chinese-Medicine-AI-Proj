package com.example.traditional_chinese_medicine_ai_proj.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.adapter.RecordAdapter
import com.example.traditional_chinese_medicine_ai_proj.data.MedicalRecord
import com.example.traditional_chinese_medicine_ai_proj.utils.MockDataLoader
import com.example.traditional_chinese_medicine_ai_proj.utils.AppointmentManager

/**
 * 就诊记录Fragment
 * 展示历史就诊记录和治疗进度
 */
class RecordFragment : Fragment() {

    private lateinit var recyclerRecords: RecyclerView

    private lateinit var recordAdapter: RecordAdapter
    private val records = mutableListOf<MedicalRecord>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        loadData()
    }

    private fun initViews(view: View) {
        recyclerRecords = view.findViewById(R.id.recyclerRecords)

        // 设置RecyclerView
        recordAdapter = RecordAdapter(records)
        recyclerRecords.layoutManager = LinearLayoutManager(requireContext())
        recyclerRecords.adapter = recordAdapter
    }

    private fun loadData() {
        records.clear()

        // 先加载预约记录（待诊断）
        val appointments = AppointmentManager.getAppointments(requireContext())
        records.addAll(appointments)

        // 再加载历史就诊记录
        val historyRecords = MockDataLoader.loadRecords(requireContext())
        records.addAll(historyRecords)

        recordAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        // 每次返回时刷新数据，确保显示最新的预约记录
        loadData()
    }
}

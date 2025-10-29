package com.example.traditional_chinese_medicine_ai_proj.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.DoctorDetailActivity
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.adapter.DoctorListAdapter
import com.example.traditional_chinese_medicine_ai_proj.data.Doctor
import com.example.traditional_chinese_medicine_ai_proj.utils.MockDataLoader

/**
 * 医师介绍Fragment
 * 展示医师列表并支持筛选
 */
class DoctorFragment : Fragment() {

    private lateinit var spinnerDept: Spinner
    private lateinit var recyclerDoctors: RecyclerView

    private lateinit var doctorAdapter: DoctorListAdapter
    private val doctors = mutableListOf<Doctor>()
    private val allDoctors = mutableListOf<Doctor>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_doctor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        loadData()
        setupListeners()
    }

    private fun initViews(view: View) {
        spinnerDept = view.findViewById(R.id.spinnerDept)
        recyclerDoctors = view.findViewById(R.id.recyclerDoctors)

        // 设置RecyclerView
        doctorAdapter = DoctorListAdapter(doctors) { doctor ->
            onDoctorClicked(doctor)
        }
        recyclerDoctors.layoutManager = LinearLayoutManager(requireContext())
        recyclerDoctors.adapter = doctorAdapter
    }

    private fun loadData() {
        // 加载医师数据
        val doctorList = MockDataLoader.loadDoctors(requireContext())
        allDoctors.clear()
        allDoctors.addAll(doctorList)
        doctors.clear()
        doctors.addAll(doctorList)
        doctorAdapter.notifyDataSetChanged()

        // 设置科室筛选器
        val depts = mutableListOf("全部科室")
        depts.addAll(MockDataLoader.getAllDepts(requireContext()))

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            depts
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDept.adapter = adapter
    }

    private fun setupListeners() {
        spinnerDept.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedDept = parent?.getItemAtPosition(position).toString()
                filterDoctors(selectedDept)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun filterDoctors(dept: String) {
        doctors.clear()
        if (dept == "全部科室") {
            doctors.addAll(allDoctors)
        } else {
            doctors.addAll(allDoctors.filter { it.dept == dept })
        }
        doctorAdapter.notifyDataSetChanged()
    }

    private fun onDoctorClicked(doctor: Doctor) {
        val intent = Intent(requireContext(), DoctorDetailActivity::class.java)
        intent.putExtra("DOCTOR_ID", doctor.id)
        startActivity(intent)
    }
}

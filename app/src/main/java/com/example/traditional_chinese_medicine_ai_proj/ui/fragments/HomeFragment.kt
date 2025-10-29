package com.example.traditional_chinese_medicine_ai_proj.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.ChatActivity
import com.example.traditional_chinese_medicine_ai_proj.HandDetectActivity
import com.example.traditional_chinese_medicine_ai_proj.LectureListActivity
import com.example.traditional_chinese_medicine_ai_proj.MedicalRecordsActivity
import com.example.traditional_chinese_medicine_ai_proj.ReportActivity
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.data.Lecture
import com.example.traditional_chinese_medicine_ai_proj.adapter.LectureAdapter
import com.example.traditional_chinese_medicine_ai_proj.utils.MockDataLoader
import com.example.traditional_chinese_medicine_ai_proj.utils.PointsManager

/**
 * 首页Fragment
 * 整合个人中心、健康卡片、讲座推荐、快捷入口
 */
class HomeFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserInfo: TextView
    private lateinit var tvPoints: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvRecordsSummary: TextView
    private lateinit var layoutPointsSection: LinearLayout
    private lateinit var layoutRecordsSection: LinearLayout
    private lateinit var layoutReportSection: LinearLayout
    private lateinit var recyclerLectures: RecyclerView
    private lateinit var btnViewAllLectures: Button

    private lateinit var lectureAdapter: LectureAdapter
    private val lectures = mutableListOf<Lecture>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        loadData()
        setupListeners()
    }

    private fun initViews(view: View) {
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserInfo = view.findViewById(R.id.tvUserInfo)
        tvPoints = view.findViewById(R.id.tvPoints)
        tvLevel = view.findViewById(R.id.tvLevel)
        tvRecordsSummary = view.findViewById(R.id.tvRecordsSummary)
        layoutPointsSection = view.findViewById(R.id.layoutPointsSection)
        layoutRecordsSection = view.findViewById(R.id.layoutRecordsSection)
        layoutReportSection = view.findViewById(R.id.layoutReportSection)
        recyclerLectures = view.findViewById(R.id.recyclerLectures)
        btnViewAllLectures = view.findViewById(R.id.btnViewAllLectures)

        // 设置讲座RecyclerView
        lectureAdapter = LectureAdapter(lectures) { lecture ->
            onLectureClicked(lecture)
        }
        recyclerLectures.layoutManager = LinearLayoutManager(requireContext())
        recyclerLectures.adapter = lectureAdapter
    }

    private fun loadData() {
        // 加载用户信息
        val user = MockDataLoader.loadUser(requireContext())
        tvUserName.text = "你好，${user?.name ?: "用户"}"

        val infoText = buildString {
            user?.let {
                if (it.gender.isNotEmpty()) append("${it.gender} | ")
                if (it.age > 0) append("${it.age}岁")
                if (it.constitution.isNotEmpty()) append("\n体质：${it.constitution}")
            }
        }
        tvUserInfo.text = infoText.ifEmpty { "完善个人信息" }

        // 加载积分和等级
        val points = PointsManager.getPoints(requireContext())
        val level = PointsManager.getLevel(requireContext())
        tvPoints.text = "$points"
        tvLevel.text = level

        // 加载就诊记录统计
        val records = MockDataLoader.loadRecords(requireContext())
        val totalCount = records.size
        val ongoingCount = records.count { it.progress == "进行中" }
        tvRecordsSummary.text = "共${totalCount}条记录，${ongoingCount}条进行中"

        // 加载即将开始的讲座（最多3个）
        val upcomingLectures = MockDataLoader.getUpcomingLectures(requireContext(), 3)
        lectures.clear()
        lectures.addAll(upcomingLectures)
        lectureAdapter.notifyDataSetChanged()
    }

    private fun setupListeners() {
        btnViewAllLectures.setOnClickListener {
            startActivity(Intent(requireContext(), LectureListActivity::class.java))
        }

        layoutPointsSection.setOnClickListener {
            // 可以跳转到积分详情页或商城
            (activity as? com.example.traditional_chinese_medicine_ai_proj.MainActivity)
                ?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
                ?.selectedItemId = R.id.nav_store
        }

        layoutRecordsSection.setOnClickListener {
            // 跳转到就诊记录页面
            startActivity(Intent(requireContext(), MedicalRecordsActivity::class.java))
        }

        layoutReportSection.setOnClickListener {
            // 跳转到病情上报页面
            startActivity(Intent(requireContext(), ReportActivity::class.java))
        }
    }

    private fun onLectureClicked(lecture: Lecture) {
        Toast.makeText(
            requireContext(),
            "讲座：${lecture.title}",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onResume() {
        super.onResume()
        // 刷新积分和等级显示
        val points = PointsManager.getPoints(requireContext())
        val level = PointsManager.getLevel(requireContext())
        tvPoints.text = "$points"
        tvLevel.text = level
    }
}

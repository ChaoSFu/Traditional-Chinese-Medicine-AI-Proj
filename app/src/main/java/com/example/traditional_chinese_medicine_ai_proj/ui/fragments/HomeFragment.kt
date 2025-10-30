package com.example.traditional_chinese_medicine_ai_proj.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.traditional_chinese_medicine_ai_proj.LectureDetailActivity
import com.example.traditional_chinese_medicine_ai_proj.MedicalRecordsActivity
import com.example.traditional_chinese_medicine_ai_proj.ReportActivity
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.utils.MockDataLoader
import com.example.traditional_chinese_medicine_ai_proj.utils.PointsManager

/**
 * 首页Fragment
 * 整合个人中心、健康卡片、快捷入口
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
    private lateinit var cardCommunity: com.google.android.material.card.MaterialCardView

    // 社区相关
    private lateinit var tvTodayPosts: TextView
    private lateinit var tvActiveUsers: TextView
    private lateinit var tvUpcomingLectures: TextView
    private lateinit var layoutUpcomingLecture: LinearLayout
    private lateinit var tvUpcomingLectureTitle: TextView
    private lateinit var tvUpcomingLectureTime: TextView
    private lateinit var tvHotPost1: TextView
    private lateinit var tvHotPost2: TextView
    private lateinit var tvHotPost3: TextView

    private var upcomingLectureId: Int = -1

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
        cardCommunity = view.findViewById(R.id.cardCommunity)

        // 社区相关
        tvTodayPosts = view.findViewById(R.id.tvTodayPosts)
        tvActiveUsers = view.findViewById(R.id.tvActiveUsers)
        tvUpcomingLectures = view.findViewById(R.id.tvUpcomingLectures)
        layoutUpcomingLecture = view.findViewById(R.id.layoutUpcomingLecture)
        tvUpcomingLectureTitle = view.findViewById(R.id.tvUpcomingLectureTitle)
        tvUpcomingLectureTime = view.findViewById(R.id.tvUpcomingLectureTime)
        tvHotPost1 = view.findViewById(R.id.tvHotPost1)
        tvHotPost2 = view.findViewById(R.id.tvHotPost2)
        tvHotPost3 = view.findViewById(R.id.tvHotPost3)
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

        // 加载社区数据
        loadCommunityData()
    }

    private fun loadCommunityData() {
        // 加载帖子数据
        val posts = MockDataLoader.loadPosts(requireContext())

        // 模拟今日新帖数（取总数的20%）
        val todayPostsCount = (posts.size * 0.2).toInt().coerceAtLeast(1)
        tvTodayPosts.text = todayPostsCount.toString()

        // 模拟活跃用户数（基于帖子数 * 3）
        val activeUsersCount = posts.size * 3
        tvActiveUsers.text = activeUsersCount.toString()

        // 加载即将开始的讲座
        val lectures = MockDataLoader.loadLectures(requireContext())
        val upcomingLectures = lectures.filter { it.status == "upcoming" }
        tvUpcomingLectures.text = upcomingLectures.size.toString()

        if (upcomingLectures.isNotEmpty()) {
            val lecture = upcomingLectures.first()
            upcomingLectureId = lecture.id
            tvUpcomingLectureTitle.text = lecture.title
            tvUpcomingLectureTime.text = "${lecture.date} ${lecture.time.split("-").first()} • ${lecture.speaker}"
        }

        // 加载热门帖子（按点赞数排序）
        val hotPosts = posts.sortedByDescending { it.likes }.take(3)
        if (hotPosts.size >= 1) {
            tvHotPost1.text = hotPosts[0].title
        }
        if (hotPosts.size >= 2) {
            tvHotPost2.text = hotPosts[1].title
        }
        if (hotPosts.size >= 3) {
            tvHotPost3.text = hotPosts[2].title
        }
    }

    private fun setupListeners() {

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

        cardCommunity.setOnClickListener {
            // 跳转到中医交流社区
            startActivity(Intent(requireContext(), com.example.traditional_chinese_medicine_ai_proj.CommunityActivity::class.java))
        }

        // 点击讲座预告跳转详情
        layoutUpcomingLecture.setOnClickListener {
            if (upcomingLectureId != -1) {
                val intent = Intent(requireContext(), LectureDetailActivity::class.java)
                intent.putExtra("LECTURE_ID", upcomingLectureId)
                startActivity(intent)
            }
        }
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

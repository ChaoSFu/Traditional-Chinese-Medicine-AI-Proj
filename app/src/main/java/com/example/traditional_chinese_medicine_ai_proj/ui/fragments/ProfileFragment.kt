package com.example.traditional_chinese_medicine_ai_proj.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.traditional_chinese_medicine_ai_proj.ChatActivity
import com.example.traditional_chinese_medicine_ai_proj.HandDetectActivity
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.ReportActivity
import com.example.traditional_chinese_medicine_ai_proj.utils.MockDataLoader
import com.example.traditional_chinese_medicine_ai_proj.utils.PointsManager

/**
 * 个人中心/AI助手Fragment
 * 集成个人信息、AI问诊、积分系统等
 */
class ProfileFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserInfo: TextView
    private lateinit var tvPoints: TextView
    private lateinit var tvLevel: TextView
    private lateinit var btnAiChat: Button
    private lateinit var btnHandDetect: Button
    private lateinit var btnReport: Button
    private lateinit var layoutPointsSection: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
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
        btnAiChat = view.findViewById(R.id.btnAiChat)
        btnHandDetect = view.findViewById(R.id.btnHandDetect)
        btnReport = view.findViewById(R.id.btnReport)
        layoutPointsSection = view.findViewById(R.id.layoutPointsSection)
    }

    private fun loadData() {
        // 加载用户信息
        val user = MockDataLoader.loadUser(requireContext())
        tvUserName.text = user?.name ?: "用户"

        val infoText = buildString {
            user?.let {
                if (it.gender.isNotEmpty()) append("${it.gender} | ")
                if (it.age > 0) append("${it.age}岁\n")
                if (it.constitution.isNotEmpty()) append("体质：${it.constitution}")
            }
        }
        tvUserInfo.text = infoText

        // 加载积分
        val points = PointsManager.getPoints(requireContext())
        val level = PointsManager.getLevel(requireContext())
        tvPoints.text = "$points"
        tvLevel.text = level
    }

    private fun setupListeners() {
        btnAiChat.setOnClickListener {
            startActivity(Intent(requireContext(), ChatActivity::class.java))
        }

        btnHandDetect.setOnClickListener {
            startActivity(Intent(requireContext(), HandDetectActivity::class.java))
        }

        btnReport.setOnClickListener {
            startActivity(Intent(requireContext(), ReportActivity::class.java))
        }

        layoutPointsSection.setOnClickListener {
            // 可以跳转到积分详情页
            android.widget.Toast.makeText(
                requireContext(),
                "积分详情功能开发中...",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // 刷新积分显示
        val points = PointsManager.getPoints(requireContext())
        val level = PointsManager.getLevel(requireContext())
        tvPoints.text = "$points"
        tvLevel.text = level
    }
}

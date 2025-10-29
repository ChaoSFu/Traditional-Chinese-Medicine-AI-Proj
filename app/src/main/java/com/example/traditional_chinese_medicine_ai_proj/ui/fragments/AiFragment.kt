package com.example.traditional_chinese_medicine_ai_proj.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.traditional_chinese_medicine_ai_proj.ChatActivity
import com.example.traditional_chinese_medicine_ai_proj.HandDetectActivity
import com.example.traditional_chinese_medicine_ai_proj.R
import com.google.android.material.card.MaterialCardView

/**
 * AI助手Fragment
 * 包含AI问诊和穴位识别功能
 */
class AiFragment : Fragment() {

    private lateinit var cardAiChat: MaterialCardView
    private lateinit var cardHandDetect: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ai, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupListeners()
    }

    private fun initViews(view: View) {
        cardAiChat = view.findViewById(R.id.cardAiChat)
        cardHandDetect = view.findViewById(R.id.cardHandDetect)
    }

    private fun setupListeners() {
        // AI问诊卡片点击
        cardAiChat.setOnClickListener {
            startActivity(Intent(requireContext(), ChatActivity::class.java))
        }

        // 穴位识别卡片点击
        cardHandDetect.setOnClickListener {
            startActivity(Intent(requireContext(), HandDetectActivity::class.java))
        }
    }
}

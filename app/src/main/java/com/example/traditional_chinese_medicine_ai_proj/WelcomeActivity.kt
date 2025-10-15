package com.example.traditional_chinese_medicine_ai_proj

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * 欢迎页面 / 主页
 * Welcome Activity - Introduction and Start
 */
class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // 初始化视图
        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnAiChat = findViewById<Button>(R.id.btnAiChat)
        val btnAbout = findViewById<Button>(R.id.btnAbout)

        // 穴位定位按钮 - 进入身体部位选择
        btnStart.setOnClickListener {
            val intent = Intent(this, BodyPartSelectionActivity::class.java)
            startActivity(intent)
        }

        // AI 问答按钮 - 进入智能问答
        btnAiChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        // 关于按钮 - 显示应用信息
        btnAbout.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun showAboutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("关于应用")
            .setMessage("""
                中医穴位定位 v1.0

                本应用结合 AI 视觉识别和智能问答技术，为您提供专业的中医穴位定位与知识咨询服务。

                📍 穴位定位功能：
                • 基于 MediaPipe 实时手部识别
                • 精准标注手部穴位位置
                • 离线运行，保护隐私
                • 详细的穴位功效说明

                🤖 AI 智能问答：
                • 基于 ChatGPT 的中医知识库
                • 支持穴位、经络、症状咨询
                • 多模型选择（GPT-3.5/GPT-4）
                • 可自定义参数个性化回答

                ⚠️ 免责声明：
                本应用仅供学习参考，不能替代专业医疗建议。如有健康问题，请咨询专业医师。

                💡 技术支持：
                • 穴位定位：MediaPipe Hands
                • 智能问答：OpenAI ChatGPT API
            """.trimIndent())
            .setPositiveButton("确定", null)
            .show()
    }
}

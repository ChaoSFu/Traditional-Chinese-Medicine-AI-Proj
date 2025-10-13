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
        val btnAbout = findViewById<Button>(R.id.btnAbout)

        // 开始按钮 - 进入身体部位选择
        btnStart.setOnClickListener {
            val intent = Intent(this, BodyPartSelectionActivity::class.java)
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
                中医穴位实时定位 v1.0

                本应用基于 MediaPipe 技术，通过摄像头实时识别人体关键点，精准定位中医穴位。

                功能特点：
                • 实时手部穴位定位
                • 离线运行，保护隐私
                • 准确的穴位标注
                • 详细的功效说明

                免责声明：
                本应用仅供学习参考，不能替代专业医疗建议。

                技术支持：
                基于 MediaPipe Hands
            """.trimIndent())
            .setPositiveButton("确定", null)
            .show()
    }
}

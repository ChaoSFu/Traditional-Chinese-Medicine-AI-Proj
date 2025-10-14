package com.example.traditional_chinese_medicine_ai_proj

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.data.AcupointType
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

/**
 * 穴位选择页面
 * Acupoint Selection Activity
 */
class AcupointSelectionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AcupointAdapter
    private lateinit var btnStartDetection: Button
    private val selectedAcupoints = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acupoint_selection)

        val bodyPart = intent.getStringExtra("BODY_PART") ?: "hand"

        // 设置工具栏
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = when (bodyPart) {
            "hand" -> "手部穴位选择"
            else -> "穴位选择"
        }

        // 初始化视图
        recyclerView = findViewById(R.id.recyclerViewAcupoints)
        btnStartDetection = findViewById(R.id.btnStartDetection)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // 加载穴位数据
        loadAcupoints(bodyPart)

        // 开始定位按钮
        btnStartDetection.setOnClickListener {
            startDetection()
        }

        // 默认选中所有穴位（手背2个，手心2个）
        selectedAcupoints.addAll(listOf("LI4", "LI5", "PC8", "HT8"))
        updateButtonState()
    }

    private fun loadAcupoints(bodyPart: String) {
        when (bodyPart) {
            "hand" -> loadHandAcupoints()
            else -> {
                // 其他部位暂无数据
                finish()
            }
        }
    }

    private fun loadHandAcupoints() {
        try {
            // 从 assets 读取穴位数据
            val inputStream = assets.open("acupoints.json")
            val reader = InputStreamReader(inputStream)
            val gson = Gson()

            val type = object : TypeToken<AcupointData>() {}.type
            val acupointData: AcupointData = gson.fromJson(reader, type)

            reader.close()

            // 创建适配器
            adapter = AcupointAdapter(acupointData.acupoints, selectedAcupoints) {
                updateButtonState()
            }
            recyclerView.adapter = adapter

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateButtonState() {
        btnStartDetection.isEnabled = selectedAcupoints.isNotEmpty()
        btnStartDetection.text = if (selectedAcupoints.isEmpty()) {
            "请选择至少一个穴位"
        } else {
            "开始定位 (${selectedAcupoints.size}个穴位)"
        }
    }

    private fun startDetection() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putStringArrayListExtra("SELECTED_ACUPOINTS", ArrayList(selectedAcupoints))
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /**
     * 穴位数据容器
     */
    data class AcupointData(
        val acupoints: List<AcupointInfo>
    )

    /**
     * 穴位信息
     */
    data class AcupointInfo(
        val id: String,
        val nameCn: String,
        val nameEn: String,
        val meridian: String,
        val location: String,
        val functions: List<String>
    )
}

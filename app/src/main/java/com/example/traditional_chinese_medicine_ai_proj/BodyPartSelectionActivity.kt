package com.example.traditional_chinese_medicine_ai_proj

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

/**
 * 身体部位选择页面
 * Body Part Selection Activity
 */
class BodyPartSelectionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BodyPartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_body_part_selection)

        // 设置工具栏
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "中医穴位定位 - 选择部位"

        // 初始化 RecyclerView
        recyclerView = findViewById(R.id.recyclerViewBodyParts)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2列网格布局

        // 准备身体部位数据
        val bodyParts = listOf(
            BodyPart("hand", "手部", "已支持", true, R.drawable.ic_hand),
            BodyPart("foot", "足部", "即将推出", false, R.drawable.ic_foot),
            BodyPart("ear", "耳部", "即将推出", false, R.drawable.ic_ear),
            BodyPart("head", "头部", "即将推出", false, R.drawable.ic_head),
            BodyPart("back", "背部", "即将推出", false, R.drawable.ic_back),
            BodyPart("abdomen", "腹部", "即将推出", false, R.drawable.ic_abdomen)
        )

        // 设置适配器
        adapter = BodyPartAdapter(bodyParts) { bodyPart ->
            onBodyPartSelected(bodyPart)
        }
        recyclerView.adapter = adapter
    }

    private fun onBodyPartSelected(bodyPart: BodyPart) {
        if (bodyPart.isAvailable) {
            // 如果可用，跳转到穴位选择页面
            val intent = Intent(this, AcupointSelectionActivity::class.java)
            intent.putExtra("BODY_PART", bodyPart.id)
            startActivity(intent)
        } else {
            // 如果不可用，显示提示
            Toast.makeText(this, "${bodyPart.name}功能${bodyPart.status}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /**
     * 身体部位数据类
     */
    data class BodyPart(
        val id: String,
        val name: String,
        val status: String,
        val isAvailable: Boolean,
        val iconRes: Int
    )
}

package com.example.traditional_chinese_medicine_ai_proj

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.traditional_chinese_medicine_ai_proj.utils.MockDataLoader

/**
 * 科室详情Activity
 */
class DepartmentDetailActivity : AppCompatActivity() {

    private lateinit var tvDeptName: TextView
    private lateinit var tvEnglishName: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvServices: TextView
    private lateinit var tvTreatments: TextView
    private lateinit var tvConditions: TextView
    private lateinit var tvFeatures: TextView
    private lateinit var tvDoctorCount: TextView
    private lateinit var btnBack: Button
    private lateinit var btnViewDoctors: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_department_detail)

        initViews()
        loadData()
        setupListeners()
    }

    private fun initViews() {
        tvDeptName = findViewById(R.id.tvDeptName)
        tvEnglishName = findViewById(R.id.tvEnglishName)
        tvDescription = findViewById(R.id.tvDescription)
        tvServices = findViewById(R.id.tvServices)
        tvTreatments = findViewById(R.id.tvTreatments)
        tvConditions = findViewById(R.id.tvConditions)
        tvFeatures = findViewById(R.id.tvFeatures)
        tvDoctorCount = findViewById(R.id.tvDoctorCount)
        btnBack = findViewById(R.id.btnBack)
        btnViewDoctors = findViewById(R.id.btnViewDoctors)
    }

    private fun loadData() {
        val deptName = intent.getStringExtra("DEPT_NAME")
        if (deptName == null) {
            Toast.makeText(this, "科室信息不存在", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val department = MockDataLoader.getDepartmentByName(this, deptName)
        if (department == null) {
            Toast.makeText(this, "科室信息不存在", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvDeptName.text = department.name
        tvEnglishName.text = department.englishName
        tvDescription.text = department.description

        // 显示服务项目
        tvServices.text = "• ${department.services.joinToString("\n• ")}"

        // 显示治疗方式
        tvTreatments.text = "• ${department.treatments.joinToString("\n• ")}"

        // 显示适应症
        tvConditions.text = "• ${department.conditions.joinToString("\n• ")}"

        // 显示科室特色
        tvFeatures.text = "• ${department.features.joinToString("\n• ")}"

        // 显示医师数量
        tvDoctorCount.text = "本科室现有医师：${department.doctorCount}位"
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnViewDoctors.setOnClickListener {
            // TODO: 跳转到医师列表页面并筛选该科室
            Toast.makeText(
                this,
                "请前往\"医师\"页面查看本科室医师",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}

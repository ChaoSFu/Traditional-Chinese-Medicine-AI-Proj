package com.example.traditional_chinese_medicine_ai_proj

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.traditional_chinese_medicine_ai_proj.utils.MockDataLoader

class DoctorDetailActivity : AppCompatActivity() {

    private lateinit var ivAvatar: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvDept: TextView
    private lateinit var tvYears: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvEducation: TextView
    private lateinit var tvSpecialty: TextView
    private lateinit var tvIntro: TextView
    private lateinit var btnAppointment: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_detail)

        initViews()
        loadDoctorData()
        setupListeners()
    }

    private fun initViews() {
        ivAvatar = findViewById(R.id.ivAvatar)
        tvName = findViewById(R.id.tvName)
        tvTitle = findViewById(R.id.tvTitle)
        tvDept = findViewById(R.id.tvDept)
        tvYears = findViewById(R.id.tvYears)
        tvRating = findViewById(R.id.tvRating)
        tvEducation = findViewById(R.id.tvEducation)
        tvSpecialty = findViewById(R.id.tvSpecialty)
        tvIntro = findViewById(R.id.tvIntro)
        btnAppointment = findViewById(R.id.btnAppointment)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun loadDoctorData() {
        val doctorId = intent.getIntExtra("DOCTOR_ID", -1)
        if (doctorId == -1) {
            Toast.makeText(this, "医师信息不存在", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val doctor = MockDataLoader.getDoctorById(this, doctorId)
        if (doctor == null) {
            Toast.makeText(this, "医师信息不存在", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvName.text = doctor.name
        tvTitle.text = doctor.title
        tvDept.text = doctor.dept
        tvYears.text = "${doctor.years}年临床经验"
        tvRating.text = "评分：${doctor.rating}"
        tvEducation.text = doctor.education
        tvSpecialty.text = "擅长：${doctor.specialty}"
        tvIntro.text = doctor.intro

        ivAvatar.setImageResource(R.drawable.ic_doctor_avatar)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnAppointment.setOnClickListener {
            val doctorId = intent.getIntExtra("DOCTOR_ID", -1)
            if (doctorId != -1) {
                val intent = Intent(this, DoctorScheduleActivity::class.java)
                intent.putExtra("DOCTOR_ID", doctorId)
                startActivity(intent)
            }
        }
    }
}

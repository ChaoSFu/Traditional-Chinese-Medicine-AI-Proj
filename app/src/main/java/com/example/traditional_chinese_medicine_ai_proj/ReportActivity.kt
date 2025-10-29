package com.example.traditional_chinese_medicine_ai_proj

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ReportActivity : AppCompatActivity() {

    private lateinit var etSymptoms: EditText
    private lateinit var etDuration: EditText
    private lateinit var etNotes: EditText
    private lateinit var ivPhoto: ImageView
    private lateinit var btnTakePhoto: Button
    private lateinit var btnSubmit: Button
    private lateinit var btnBack: Button

    companion object {
        private const val REQUEST_CODE_CAMERA = 100
        private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etSymptoms = findViewById(R.id.etSymptoms)
        etDuration = findViewById(R.id.etDuration)
        etNotes = findViewById(R.id.etNotes)
        ivPhoto = findViewById(R.id.ivPhoto)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnTakePhoto.setOnClickListener {
            if (allPermissionsGranted()) {
                Toast.makeText(this, "拍照功能开发中...", Toast.LENGTH_SHORT).show()
                // TODO: 实现拍照功能
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_CAMERA
                )
            }
        }

        btnSubmit.setOnClickListener {
            submitReport()
        }
    }

    private fun submitReport() {
        val symptoms = etSymptoms.text.toString().trim()
        val duration = etDuration.text.toString().trim()

        if (symptoms.isEmpty()) {
            Toast.makeText(this, "请描述您的症状", Toast.LENGTH_SHORT).show()
            return
        }

        if (duration.isEmpty()) {
            Toast.makeText(this, "请填写症状持续时间", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: 保存病情上报到数据库或发送到服务器
        Toast.makeText(
            this,
            "病情上报成功！医师将尽快回复",
            Toast.LENGTH_LONG
        ).show()

        finish()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CAMERA) {
            if (allPermissionsGranted()) {
                Toast.makeText(this, "相机权限已授予", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "相机权限被拒绝", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

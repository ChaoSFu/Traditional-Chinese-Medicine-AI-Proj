package com.example.traditional_chinese_medicine_ai_proj

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.traditional_chinese_medicine_ai_proj.data.Lecture
import com.example.traditional_chinese_medicine_ai_proj.utils.MockDataLoader

class LectureDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnShare: ImageButton
    private lateinit var ivLectureCover: ImageView
    private lateinit var tvLectureTitle: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvSpeaker: TextView
    private lateinit var tvSpeakerTitle: TextView
    private lateinit var tvDateTime: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvRegistration: TextView
    private lateinit var tvDescription: TextView
    private lateinit var layoutTopics: LinearLayout
    private lateinit var tvBottomRegistration: TextView
    private lateinit var btnRegister: Button

    private var currentLecture: Lecture? = null
    private var isRegistered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lecture_detail)

        initViews()
        loadLectureDetail()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnShare = findViewById(R.id.btnShare)
        ivLectureCover = findViewById(R.id.ivLectureCover)
        tvLectureTitle = findViewById(R.id.tvLectureTitle)
        tvStatus = findViewById(R.id.tvStatus)
        tvSpeaker = findViewById(R.id.tvSpeaker)
        tvSpeakerTitle = findViewById(R.id.tvSpeakerTitle)
        tvDateTime = findViewById(R.id.tvDateTime)
        tvLocation = findViewById(R.id.tvLocation)
        tvRegistration = findViewById(R.id.tvRegistration)
        tvDescription = findViewById(R.id.tvDescription)
        layoutTopics = findViewById(R.id.layoutTopics)
        tvBottomRegistration = findViewById(R.id.tvBottomRegistration)
        btnRegister = findViewById(R.id.btnRegister)
    }

    private fun loadLectureDetail() {
        val lectureId = intent.getIntExtra("LECTURE_ID", -1)
        if (lectureId == -1) {
            Toast.makeText(this, "讲座ID无效", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentLecture = MockDataLoader.loadLectures(this).find { it.id == lectureId }
        if (currentLecture == null) {
            Toast.makeText(this, "未找到讲座信息", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        displayLecture(currentLecture!!)
    }

    private fun displayLecture(lecture: Lecture) {
        // 加载封面图片
        if (lecture.imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(lecture.imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.bg_points_card)
                .error(R.drawable.bg_points_card)
                .into(ivLectureCover)
        } else {
            ivLectureCover.setImageResource(R.drawable.bg_points_card)
        }

        tvLectureTitle.text = lecture.title
        tvSpeaker.text = lecture.speaker
        tvSpeakerTitle.text = lecture.speakerTitle
        tvDateTime.text = "${lecture.date} ${lecture.time}"
        tvLocation.text = lecture.location
        tvRegistration.text = "${lecture.registered}/${lecture.capacity} 人"
        tvDescription.text = lecture.description
        tvBottomRegistration.text = "${lecture.registered}人"

        // 根据状态设置样式
        when (lecture.status) {
            "upcoming" -> {
                tvStatus.text = "即将开始"
                tvStatus.setTextColor(getColor(R.color.tcm_primary))
                tvStatus.setBackgroundResource(R.drawable.bg_tag_category)
                btnRegister.isEnabled = true
                btnRegister.text = "立即报名"
            }
            "ongoing" -> {
                tvStatus.text = "进行中"
                tvStatus.setTextColor(getColor(R.color.tcm_accent))
                tvStatus.setBackgroundResource(R.drawable.bg_tag_default)
                btnRegister.isEnabled = false
                btnRegister.text = "已开始"
                btnRegister.alpha = 0.5f
            }
            "completed" -> {
                tvStatus.text = "已结束"
                tvStatus.setTextColor(getColor(android.R.color.darker_gray))
                tvStatus.setBackgroundResource(R.drawable.bg_tag_default)
                btnRegister.isEnabled = false
                btnRegister.text = "已结束"
                btnRegister.alpha = 0.5f
            }
            "full" -> {
                tvStatus.text = "已满员"
                tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
                tvStatus.setBackgroundResource(R.drawable.bg_tag_default)
                btnRegister.isEnabled = false
                btnRegister.text = "已满员"
                btnRegister.alpha = 0.5f
            }
        }

        // 显示讲座主题
        layoutTopics.removeAllViews()
        lecture.topics.forEach { topic ->
            val topicView = TextView(this).apply {
                text = "• $topic"
                textSize = 14f
                setTextColor(getColor(R.color.black))
                setPadding(0, 8, 0, 8)
            }
            layoutTopics.addView(topicView)
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnShare.setOnClickListener {
            Toast.makeText(this, "分享功能开发中...", Toast.LENGTH_SHORT).show()
        }

        btnRegister.setOnClickListener {
            handleRegistration()
        }
    }

    private fun handleRegistration() {
        val lecture = currentLecture ?: return

        if (isRegistered) {
            // 取消报名
            Toast.makeText(this, "取消报名成功", Toast.LENGTH_SHORT).show()
            isRegistered = false
            btnRegister.text = "立即报名"

            // 更新报名人数显示
            val newCount = lecture.registered - 1
            tvRegistration.text = "$newCount/${lecture.capacity} 人"
            tvBottomRegistration.text = "${newCount}人"
        } else {
            // 报名
            if (lecture.registered >= lecture.capacity) {
                Toast.makeText(this, "报名人数已满", Toast.LENGTH_SHORT).show()
                return
            }

            Toast.makeText(this, "报名成功！我们将通过短信通知您活动详情", Toast.LENGTH_LONG).show()
            isRegistered = true
            btnRegister.text = "取消报名"

            // 更新报名人数显示
            val newCount = lecture.registered + 1
            tvRegistration.text = "$newCount/${lecture.capacity} 人"
            tvBottomRegistration.text = "${newCount}人"
        }
    }
}

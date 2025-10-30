package com.example.traditional_chinese_medicine_ai_proj

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.adapter.LectureAdapter
import com.example.traditional_chinese_medicine_ai_proj.data.Lecture
import com.example.traditional_chinese_medicine_ai_proj.utils.MockDataLoader

/**
 * 讲座与活动列表Activity
 */
class LectureListActivity : AppCompatActivity() {

    private lateinit var recyclerLectures: RecyclerView
    private lateinit var btnBack: Button

    private lateinit var lectureAdapter: LectureAdapter
    private val lectures = mutableListOf<Lecture>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lecture_list)

        initViews()
        loadData()
        setupListeners()
    }

    private fun initViews() {
        recyclerLectures = findViewById(R.id.recyclerLectures)
        btnBack = findViewById(R.id.btnBack)

        // 设置RecyclerView
        lectureAdapter = LectureAdapter(lectures) { lecture ->
            onLectureClicked(lecture)
        }
        recyclerLectures.layoutManager = LinearLayoutManager(this)
        recyclerLectures.adapter = lectureAdapter
    }

    private fun loadData() {
        val lectureList = MockDataLoader.loadLectures(this)
        lectures.clear()
        lectures.addAll(lectureList)
        lectureAdapter.notifyDataSetChanged()
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun onLectureClicked(lecture: Lecture) {
        val intent = Intent(this, LectureDetailActivity::class.java)
        intent.putExtra("LECTURE_ID", lecture.id)
        startActivity(intent)
    }
}

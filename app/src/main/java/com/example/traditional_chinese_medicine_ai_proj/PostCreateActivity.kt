package com.example.traditional_chinese_medicine_ai_proj

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.traditional_chinese_medicine_ai_proj.data.Post
import com.example.traditional_chinese_medicine_ai_proj.utils.MockDataLoader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostCreateActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnPublish: Button
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText

    private lateinit var tagPatient: TextView
    private lateinit var tagWellness: TextView
    private lateinit var tagDiet: TextView
    private lateinit var tagMeridian: TextView
    private lateinit var tagDoctor: TextView

    private var selectedCategory: String = ""
    private val categoryTags = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_create)

        initViews()
        setupCategorySelection()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnPublish = findViewById(R.id.btnPublish)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)

        tagPatient = findViewById(R.id.tagPatient)
        tagWellness = findViewById(R.id.tagWellness)
        tagDiet = findViewById(R.id.tagDiet)
        tagMeridian = findViewById(R.id.tagMeridian)
        tagDoctor = findViewById(R.id.tagDoctor)

        categoryTags.addAll(listOf(tagPatient, tagWellness, tagDiet, tagMeridian, tagDoctor))
    }

    private fun setupCategorySelection() {
        val categories = mapOf(
            tagPatient to "病友交流",
            tagWellness to "养生调理",
            tagDiet to "药膳食疗",
            tagMeridian to "经络与穴位",
            tagDoctor to "医生分享"
        )

        categories.forEach { (tag, category) ->
            tag.setOnClickListener {
                selectCategory(tag, category)
            }
        }
    }

    private fun selectCategory(selectedTag: TextView, category: String) {
        // 重置所有标签样式
        categoryTags.forEach { tag ->
            tag.setBackgroundResource(R.drawable.bg_tag_default)
            tag.setTextColor(getColor(android.R.color.darker_gray))
        }

        // 设置选中标签样式
        selectedTag.setBackgroundResource(R.drawable.bg_tag_category)
        selectedTag.setTextColor(getColor(R.color.tcm_primary_dark))

        selectedCategory = category
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnPublish.setOnClickListener {
            publishPost()
        }
    }

    private fun publishPost() {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()

        // 验证输入
        when {
            title.isEmpty() -> {
                Toast.makeText(this, "请输入帖子标题", Toast.LENGTH_SHORT).show()
                etTitle.requestFocus()
                return
            }
            title.length < 5 -> {
                Toast.makeText(this, "标题至少需要5个字符", Toast.LENGTH_SHORT).show()
                etTitle.requestFocus()
                return
            }
            content.isEmpty() -> {
                Toast.makeText(this, "请输入帖子内容", Toast.LENGTH_SHORT).show()
                etContent.requestFocus()
                return
            }
            content.length < 10 -> {
                Toast.makeText(this, "内容至少需要10个字符", Toast.LENGTH_SHORT).show()
                etContent.requestFocus()
                return
            }
            selectedCategory.isEmpty() -> {
                Toast.makeText(this, "请选择话题分类", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // 创建新帖子
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val newPost = Post(
            id = generateNewPostId(),
            author = "我",
            role = "病友",
            title = title,
            content = content,
            likes = 0,
            comments = 0,
            views = 0,
            category = selectedCategory,
            time = currentTime,
            isLiked = false,
            isCollected = false
        )

        // 将新帖子添加到数据源
        MockDataLoader.addPost(newPost)

        Toast.makeText(this, "发布成功 ✓", Toast.LENGTH_SHORT).show()

        // 返回社区页面
        finish()
    }

    private fun generateNewPostId(): Int {
        // 获取当前最大的帖子ID并加1
        val currentPosts = MockDataLoader.loadPosts(this)
        return if (currentPosts.isEmpty()) {
            1
        } else {
            currentPosts.maxOf { it.id } + 1
        }
    }
}

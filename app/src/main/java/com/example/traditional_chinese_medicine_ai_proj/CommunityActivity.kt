package com.example.traditional_chinese_medicine_ai_proj

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.adapter.LectureHorizontalAdapter
import com.example.traditional_chinese_medicine_ai_proj.adapter.PostAdapter
import com.example.traditional_chinese_medicine_ai_proj.data.Lecture
import com.example.traditional_chinese_medicine_ai_proj.data.Post
import com.example.traditional_chinese_medicine_ai_proj.utils.MockDataLoader
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

/**
 * 中医交流社区Activity
 */
class CommunityActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnSearch: ImageButton
    private lateinit var tabCategories: TabLayout
    private lateinit var tvHot: TextView
    private lateinit var tvLatest: TextView
    private lateinit var layoutLectureHeader: LinearLayout
    private lateinit var ivToggleLectures: ImageView
    private lateinit var recyclerLectures: RecyclerView
    private lateinit var tvViewAllLectures: TextView
    private lateinit var recyclerPosts: RecyclerView
    private lateinit var fabCreatePost: FloatingActionButton

    private lateinit var lectureAdapter: LectureHorizontalAdapter
    private lateinit var postAdapter: PostAdapter
    private val lectures = mutableListOf<Lecture>()
    private val posts = mutableListOf<Post>()
    private var currentCategory = "全部"
    private var isHotMode = true
    private var isLecturesExpanded = true

    // 所有话题分类
    private val categories = listOf(
        "全部", "病友交流", "养生调理", "药膳食疗", "经络与穴位", "医生分享"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        initViews()
        setupTabs()
        loadLectures()
        loadPosts()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        // 从发帖页返回时刷新列表
        loadPosts()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnSearch = findViewById(R.id.btnSearch)
        tabCategories = findViewById(R.id.tabCategories)
        tvHot = findViewById(R.id.tvHot)
        tvLatest = findViewById(R.id.tvLatest)
        layoutLectureHeader = findViewById(R.id.layoutLectureHeader)
        ivToggleLectures = findViewById(R.id.ivToggleLectures)
        recyclerLectures = findViewById(R.id.recyclerLectures)
        tvViewAllLectures = findViewById(R.id.tvViewAllLectures)
        recyclerPosts = findViewById(R.id.recyclerPosts)
        fabCreatePost = findViewById(R.id.fabCreatePost)

        // 设置讲座横向RecyclerView
        lectureAdapter = LectureHorizontalAdapter(lectures) { lecture ->
            onLectureClicked(lecture)
        }
        recyclerLectures.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerLectures.adapter = lectureAdapter

        // 设置帖子RecyclerView
        postAdapter = PostAdapter(posts) { post ->
            onPostClicked(post)
        }
        recyclerPosts.layoutManager = LinearLayoutManager(this)
        recyclerPosts.adapter = postAdapter
    }

    private fun setupTabs() {
        // 添加所有分类Tab
        categories.forEach { category ->
            tabCategories.addTab(tabCategories.newTab().setText(category))
        }
    }

    private fun loadLectures() {
        lectures.clear()
        // 加载即将开始的讲座（最多显示5个）
        lectures.addAll(MockDataLoader.getUpcomingLectures(this, 5))
        lectureAdapter.notifyDataSetChanged()
    }

    private fun loadPosts() {
        posts.clear()

        // 根据分类加载帖子
        val allPosts = if (currentCategory == "全部") {
            MockDataLoader.loadPosts(this)
        } else {
            MockDataLoader.getPostsByCategory(this, currentCategory)
        }

        // 根据热门/最新排序
        val sortedPosts = if (isHotMode) {
            // 热门：按点赞数+评论数排序
            allPosts.sortedByDescending { it.likes + it.comments }
        } else {
            // 最新：按时间排序（这里简单地按原顺序，实际应按时间解析）
            allPosts
        }

        posts.addAll(sortedPosts)
        postAdapter.notifyDataSetChanged()
    }

    private fun setupListeners() {
        // 返回按钮
        btnBack.setOnClickListener {
            finish()
        }

        // 搜索按钮
        btnSearch.setOnClickListener {
            Toast.makeText(this, "搜索功能开发中...", Toast.LENGTH_SHORT).show()
        }

        // Tab 切换
        tabCategories.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    currentCategory = categories[it.position]
                    loadPosts()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // 热门/最新切换
        tvHot.setOnClickListener {
            if (!isHotMode) {
                isHotMode = true
                updateSortMode()
                loadPosts()
            }
        }

        tvLatest.setOnClickListener {
            if (isHotMode) {
                isHotMode = false
                updateSortMode()
                loadPosts()
            }
        }

        // 发帖按钮
        fabCreatePost.setOnClickListener {
            startActivity(Intent(this, PostCreateActivity::class.java))
        }

        // 查看全部讲座
        tvViewAllLectures.setOnClickListener {
            startActivity(Intent(this, LectureListActivity::class.java))
        }

        // 展开/收起讲座区域
        layoutLectureHeader.setOnClickListener {
            toggleLectures()
        }
    }

    /**
     * 切换讲座区域的展开/收起状态
     */
    private fun toggleLectures() {
        isLecturesExpanded = !isLecturesExpanded

        if (isLecturesExpanded) {
            // 展开
            recyclerLectures.visibility = View.VISIBLE
            ivToggleLectures.setImageResource(android.R.drawable.arrow_up_float)
        } else {
            // 收起
            recyclerLectures.visibility = View.GONE
            ivToggleLectures.setImageResource(android.R.drawable.arrow_down_float)
        }
    }

    /**
     * 讲座点击事件
     */
    private fun onLectureClicked(lecture: Lecture) {
        val intent = Intent(this, LectureDetailActivity::class.java)
        intent.putExtra("LECTURE_ID", lecture.id)
        startActivity(intent)
    }

    /**
     * 更新排序模式的UI状态
     */
    private fun updateSortMode() {
        if (isHotMode) {
            tvHot.setTextColor(getColor(R.color.tcm_primary))
            tvHot.setTypeface(null, android.graphics.Typeface.BOLD)
            tvLatest.setTextColor(getColor(android.R.color.darker_gray))
            tvLatest.setTypeface(null, android.graphics.Typeface.NORMAL)
        } else {
            tvHot.setTextColor(getColor(android.R.color.darker_gray))
            tvHot.setTypeface(null, android.graphics.Typeface.NORMAL)
            tvLatest.setTextColor(getColor(R.color.tcm_primary))
            tvLatest.setTypeface(null, android.graphics.Typeface.BOLD)
        }
    }

    /**
     * 帖子点击事件
     */
    private fun onPostClicked(post: Post) {
        val intent = Intent(this, PostDetailActivity::class.java)
        intent.putExtra("POST_ID", post.id)
        startActivity(intent)
    }
}

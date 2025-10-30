package com.example.traditional_chinese_medicine_ai_proj

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.adapter.CommentAdapter
import com.example.traditional_chinese_medicine_ai_proj.data.Comment
import com.example.traditional_chinese_medicine_ai_proj.data.Post
import com.example.traditional_chinese_medicine_ai_proj.utils.MockDataLoader

/**
 * 帖子详情Activity
 * 显示完整帖子内容、评论列表、点赞、收藏功能
 */
class PostDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var ivAuthorAvatar: ImageView
    private lateinit var tvAuthorName: TextView
    private lateinit var tvAuthorRole: TextView
    private lateinit var tvPostTime: TextView
    private lateinit var tvPostTitle: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvPostContent: TextView
    private lateinit var tvLikesCount: TextView
    private lateinit var tvViewsCount: TextView
    private lateinit var recyclerComments: RecyclerView
    private lateinit var etComment: EditText
    private lateinit var btnLike: ImageButton
    private lateinit var btnCollect: ImageButton

    private lateinit var commentAdapter: CommentAdapter
    private val comments = mutableListOf<Comment>()

    private var currentPost: Post? = null
    private var postId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        // 获取帖子ID
        postId = intent.getIntExtra("POST_ID", 0)

        initViews()
        loadPostDetail()
        loadComments()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        ivAuthorAvatar = findViewById(R.id.ivAuthorAvatar)
        tvAuthorName = findViewById(R.id.tvAuthorName)
        tvAuthorRole = findViewById(R.id.tvAuthorRole)
        tvPostTime = findViewById(R.id.tvPostTime)
        tvPostTitle = findViewById(R.id.tvPostTitle)
        tvCategory = findViewById(R.id.tvCategory)
        tvPostContent = findViewById(R.id.tvPostContent)
        tvLikesCount = findViewById(R.id.tvLikesCount)
        tvViewsCount = findViewById(R.id.tvViewsCount)
        recyclerComments = findViewById(R.id.recyclerComments)
        etComment = findViewById(R.id.etComment)
        btnLike = findViewById(R.id.btnLike)
        btnCollect = findViewById(R.id.btnCollect)

        // 设置评论列表
        commentAdapter = CommentAdapter(comments) { comment ->
            onCommentLike(comment)
        }
        recyclerComments.layoutManager = LinearLayoutManager(this)
        recyclerComments.adapter = commentAdapter
    }

    private fun loadPostDetail() {
        currentPost = MockDataLoader.getPostById(this, postId)
        currentPost?.let { post ->
            // 显示帖子信息
            tvAuthorName.text = post.author
            tvAuthorRole.text = post.role
            tvPostTime.text = post.time
            tvPostTitle.text = post.title
            tvCategory.text = "# ${post.category}"
            tvPostContent.text = post.content
            tvLikesCount.text = "${post.likes}人点赞"
            tvViewsCount.text = "${post.views}次浏览"

            // 设置角色标签样式
            if (post.role == "医生") {
                tvAuthorRole.setBackgroundResource(R.drawable.bg_tag_premium)
            } else {
                tvAuthorRole.setBackgroundResource(R.drawable.bg_tag_default)
            }

            // 更新点赞和收藏状态
            updateLikeStatus()
            updateCollectStatus()
        }
    }

    private fun loadComments() {
        comments.clear()
        val loadedComments = MockDataLoader.getCommentsByPostId(this, postId)
        comments.addAll(loadedComments)
        commentAdapter.notifyDataSetChanged()
    }

    private fun setupListeners() {
        // 返回按钮
        btnBack.setOnClickListener {
            finish()
        }

        // 点赞按钮
        btnLike.setOnClickListener {
            currentPost?.let { post ->
                post.isLiked = !post.isLiked
                if (post.isLiked) {
                    post.likes++
                    Toast.makeText(this, "点赞成功 👍", Toast.LENGTH_SHORT).show()
                } else {
                    post.likes--
                }
                updateLikeStatus()
                tvLikesCount.text = "${post.likes}人点赞"
            }
        }

        // 收藏按钮
        btnCollect.setOnClickListener {
            currentPost?.let { post ->
                post.isCollected = !post.isCollected
                if (post.isCollected) {
                    Toast.makeText(this, "收藏成功 ⭐", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show()
                }
                updateCollectStatus()
            }
        }

        // 评论输入框点击
        etComment.setOnClickListener {
            // 弹出输入法，发送评论
            showCommentDialog()
        }
    }

    /**
     * 更新点赞按钮状态
     */
    private fun updateLikeStatus() {
        currentPost?.let { post ->
            if (post.isLiked) {
                btnLike.setColorFilter(getColor(R.color.tcm_primary))
            } else {
                btnLike.setColorFilter(getColor(android.R.color.darker_gray))
            }
        }
    }

    /**
     * 更新收藏按钮状态
     */
    private fun updateCollectStatus() {
        currentPost?.let { post ->
            if (post.isCollected) {
                btnCollect.setColorFilter(getColor(R.color.tcm_accent))
            } else {
                btnCollect.setColorFilter(getColor(android.R.color.darker_gray))
            }
        }
    }

    /**
     * 评论点赞
     */
    private fun onCommentLike(comment: Comment) {
        comment.isLiked = !comment.isLiked
        if (comment.isLiked) {
            comment.likes++
            Toast.makeText(this, "已点赞该评论", Toast.LENGTH_SHORT).show()
        } else {
            comment.likes--
        }
        commentAdapter.notifyDataSetChanged()
    }

    /**
     * 显示评论对话框
     */
    private fun showCommentDialog() {
        if (etComment.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "请输入评论内容", Toast.LENGTH_SHORT).show()
            return
        }

        // 创建新评论（mock）
        val newComment = Comment(
            id = comments.size + 1,
            postId = postId,
            user = "我",
            role = "病友",
            text = etComment.text.toString(),
            time = "刚刚",
            likes = 0,
            isLiked = false
        )

        comments.add(0, newComment)
        commentAdapter.notifyItemInserted(0)
        recyclerComments.scrollToPosition(0)

        // 清空输入框
        etComment.setText("")

        // 更新帖子评论数
        currentPost?.let {
            tvLikesCount.text = "${it.likes}人点赞 · ${comments.size}条评论"
        }

        Toast.makeText(this, "你的经验正在帮助他人 🌸", Toast.LENGTH_SHORT).show()
    }
}

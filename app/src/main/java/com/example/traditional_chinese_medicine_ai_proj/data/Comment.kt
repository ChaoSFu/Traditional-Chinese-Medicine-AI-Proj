package com.example.traditional_chinese_medicine_ai_proj.data

/**
 * 评论数据模型
 */
data class Comment(
    val id: Int,
    val postId: Int,
    val user: String,
    val userAvatar: String = "",
    val role: String = "病友",  // "病友" 或 "医生"
    val text: String,
    val time: String,
    var likes: Int = 0,
    var isLiked: Boolean = false
)

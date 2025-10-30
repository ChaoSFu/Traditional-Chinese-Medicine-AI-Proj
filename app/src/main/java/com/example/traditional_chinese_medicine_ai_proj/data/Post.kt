package com.example.traditional_chinese_medicine_ai_proj.data

/**
 * 社区帖子数据模型
 */
data class Post(
    val id: Int,
    val author: String,
    val authorAvatar: String = "",  // 头像路径（可选）
    val role: String,               // "病友" 或 "医生"
    val title: String,
    val content: String,
    val images: List<String> = emptyList(),  // 配图路径列表
    var likes: Int,
    val comments: Int,
    val views: Int = 0,             // 浏览量
    val category: String,           // 话题分类
    val time: String,
    var isLiked: Boolean = false,   // 是否已点赞
    var isCollected: Boolean = false // 是否已收藏
)

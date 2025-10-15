package com.example.traditional_chinese_medicine_ai_proj.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 聊天消息实体（用于 Room 数据库）
 */
@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String,        // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

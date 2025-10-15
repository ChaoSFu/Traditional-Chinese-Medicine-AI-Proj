package com.example.traditional_chinese_medicine_ai_proj.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 聊天消息 DAO
 */
@Dao
interface ChatDao {

    /**
     * 插入消息
     */
    @Insert
    suspend fun insertMessage(message: ChatMessage): Long

    /**
     * 批量插入消息
     */
    @Insert
    suspend fun insertMessages(messages: List<ChatMessage>)

    /**
     * 获取所有消息（按时间排序）
     */
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    suspend fun getAllMessages(): List<ChatMessage>

    /**
     * 获取所有消息（Flow，实时更新）
     */
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<ChatMessage>>

    /**
     * 获取最近 N 条消息
     */
    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<ChatMessage>

    /**
     * 删除所有消息
     */
    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()

    /**
     * 删除指定消息
     */
    @Delete
    suspend fun deleteMessage(message: ChatMessage)

    /**
     * 获取消息总数
     */
    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getMessageCount(): Int
}

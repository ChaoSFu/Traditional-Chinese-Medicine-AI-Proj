package com.example.traditional_chinese_medicine_ai_proj.repository

import com.example.traditional_chinese_medicine_ai_proj.api.MessageUI
import com.example.traditional_chinese_medicine_ai_proj.database.ChatDao
import com.example.traditional_chinese_medicine_ai_proj.database.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 聊天数据仓库
 */
class ChatRepository(private val chatDao: ChatDao) {

    /**
     * 保存消息到数据库
     */
    suspend fun saveMessage(messageUI: MessageUI) {
        val chatMessage = ChatMessage(
            role = messageUI.role,
            content = messageUI.content,
            timestamp = messageUI.timestamp
        )
        chatDao.insertMessage(chatMessage)
    }

    /**
     * 批量保存消息
     */
    suspend fun saveMessages(messages: List<MessageUI>) {
        val chatMessages = messages.map { messageUI ->
            ChatMessage(
                role = messageUI.role,
                content = messageUI.content,
                timestamp = messageUI.timestamp
            )
        }
        chatDao.insertMessages(chatMessages)
    }

    /**
     * 获取所有消息
     */
    suspend fun getAllMessages(): List<MessageUI> {
        return chatDao.getAllMessages().map { chatMessage ->
            MessageUI(
                role = chatMessage.role,
                content = chatMessage.content,
                timestamp = chatMessage.timestamp
            )
        }
    }

    /**
     * 获取所有消息（Flow，实时更新）
     */
    fun getAllMessagesFlow(): Flow<List<MessageUI>> {
        return chatDao.getAllMessagesFlow().map { messages ->
            messages.map { chatMessage ->
                MessageUI(
                    role = chatMessage.role,
                    content = chatMessage.content,
                    timestamp = chatMessage.timestamp
                )
            }
        }
    }

    /**
     * 获取最近 N 条消息
     */
    suspend fun getRecentMessages(limit: Int): List<MessageUI> {
        return chatDao.getRecentMessages(limit).reversed().map { chatMessage ->
            MessageUI(
                role = chatMessage.role,
                content = chatMessage.content,
                timestamp = chatMessage.timestamp
            )
        }
    }

    /**
     * 清空所有消息
     */
    suspend fun clearAllMessages() {
        chatDao.deleteAllMessages()
    }

    /**
     * 获取消息总数
     */
    suspend fun getMessageCount(): Int {
        return chatDao.getMessageCount()
    }
}

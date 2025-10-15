package com.example.traditional_chinese_medicine_ai_proj.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.traditional_chinese_medicine_ai_proj.api.ChatGPTClient
import com.example.traditional_chinese_medicine_ai_proj.api.Message
import com.example.traditional_chinese_medicine_ai_proj.api.MessageUI
import com.example.traditional_chinese_medicine_ai_proj.database.ChatDatabase
import com.example.traditional_chinese_medicine_ai_proj.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 中医智能问答 ViewModel
 */
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    // Repository
    private val repository: ChatRepository

    // 消息列表
    private val _messages = MutableLiveData<List<MessageUI>>(emptyList())
    val messages: LiveData<List<MessageUI>> = _messages

    // 加载状态
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // 错误信息
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // 对话历史（用于 API 调用）
    private val conversationHistory = mutableListOf<Message>()

    init {
        val chatDao = ChatDatabase.getDatabase(application).chatDao()
        repository = ChatRepository(chatDao)

        // 加载历史消息
        loadHistoryMessages()
    }

    /**
     * 加载历史消息
     */
    private fun loadHistoryMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val historyMessages = repository.getAllMessages()
                withContext(Dispatchers.Main) {
                    _messages.value = historyMessages

                    // 重建对话历史
                    conversationHistory.clear()
                    historyMessages.forEach { messageUI ->
                        conversationHistory.add(Message(messageUI.role, messageUI.content))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 发送问题
     */
    fun askQuestion(question: String, context: Context) {
        if (question.isBlank()) {
            _error.value = "请输入问题"
            return
        }

        // 读取设置
        val prefs = context.getSharedPreferences("chat_settings", Context.MODE_PRIVATE)
        val apiKey = prefs.getString("api_key", "") ?: ""
        val model = prefs.getString("model", "gpt-3.5-turbo") ?: "gpt-3.5-turbo"
        val temperature = prefs.getFloat("temperature", 0.7f)
        val maxTokens = prefs.getInt("max_tokens", 1000)

        if (apiKey.isBlank()) {
            _error.value = "请先在设置中配置 API Key"
            return
        }

        // 添加用户消息到 UI
        val userMessage = MessageUI("user", question)
        val currentMessages = _messages.value!!.toMutableList()
        currentMessages.add(userMessage)
        _messages.value = currentMessages

        // 保存用户消息到数据库
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveMessage(userMessage)
        }

        // 添加到对话历史
        conversationHistory.add(Message("user", question))

        // 开始加载
        _isLoading.value = true
        _error.value = null

        // 调用 API
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val answer = ChatGPTClient.sendMessage(
                    apiKey = apiKey,
                    userMessage = question,
                    conversationHistory = conversationHistory,
                    model = model,
                    temperature = temperature,
                    maxTokens = maxTokens
                )

                // 添加 AI 回复到对话历史
                conversationHistory.add(Message("assistant", answer))

                // 更新 UI 并保存到数据库
                withContext(Dispatchers.Main) {
                    val assistantMessage = MessageUI("assistant", answer)
                    val updated = _messages.value!!.toMutableList()
                    updated.add(assistantMessage)
                    _messages.value = updated
                    _isLoading.value = false

                    // 保存 AI 回复到数据库
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.saveMessage(assistantMessage)
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "请求失败：${e.message}"
                    _isLoading.value = false

                    // 移除最后一条用户消息和对话历史中的对应项
                    val updated = _messages.value!!.toMutableList()
                    if (updated.isNotEmpty() && updated.last().role == "user") {
                        updated.removeAt(updated.size - 1)
                        _messages.value = updated
                    }
                    if (conversationHistory.isNotEmpty() && conversationHistory.last().role == "user") {
                        conversationHistory.removeAt(conversationHistory.size - 1)
                    }
                }
            }
        }
    }

    /**
     * 清空对话
     */
    fun clearConversation() {
        _messages.value = emptyList()
        conversationHistory.clear()
        _error.value = null

        // 从数据库删除所有消息
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllMessages()
        }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _error.value = null
    }
}

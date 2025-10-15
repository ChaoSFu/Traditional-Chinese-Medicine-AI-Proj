package com.example.traditional_chinese_medicine_ai_proj

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.adapter.ChatAdapter
import com.example.traditional_chinese_medicine_ai_proj.viewmodel.ChatViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

/**
 * 中医智能问答页面
 */
class ChatActivity : AppCompatActivity() {

    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: ChatAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var inputText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var quickSettingsButton: LinearLayout
    private lateinit var settingsBar: LinearLayout
    private lateinit var tvQuickSettings: TextView
    private lateinit var tvCurrentModel: TextView
    private lateinit var tvApiKeyStatus: TextView
    private lateinit var btnCloseSettings: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // 设置工具栏
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.inflateMenu(R.menu.menu_chat)

        // 初始化视图
        recyclerView = findViewById(R.id.chatRecyclerView)
        inputText = findViewById(R.id.inputText)
        sendButton = findViewById(R.id.sendButton)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        quickSettingsButton = findViewById(R.id.quickSettingsButton)
        settingsBar = findViewById(R.id.settingsBar)
        tvQuickSettings = findViewById(R.id.tvQuickSettings)
        tvCurrentModel = findViewById(R.id.tvCurrentModel)
        tvApiKeyStatus = findViewById(R.id.tvApiKeyStatus)
        btnCloseSettings = findViewById(R.id.btnCloseSettings)

        // 设置 RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true  // 从底部开始
        }
        adapter = ChatAdapter()
        recyclerView.adapter = adapter

        // 初始化 ViewModel
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        // 观察消息列表
        viewModel.messages.observe(this) { messages ->
            adapter.submitList(messages.toList()) {
                // 滚动到最新消息
                if (messages.isNotEmpty()) {
                    recyclerView.smoothScrollToPosition(messages.size - 1)
                }
            }
        }

        // 观察加载状态
        viewModel.isLoading.observe(this) { isLoading ->
            loadingIndicator.visibility = if (isLoading) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
            sendButton.isEnabled = !isLoading
        }

        // 观察错误
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        // 发送按钮点击
        sendButton.setOnClickListener {
            sendMessage()
        }

        // 输入框回车发送
        inputText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }

        // 工具栏菜单
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_clear -> {
                    showClearConfirmDialog()
                    true
                }
                R.id.action_settings -> {
                    // 跳转到设置页面
                    val intent = Intent(this, ChatSettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // 快捷设置按钮点击 - 展开/收起设置栏或跳转到设置页面
        quickSettingsButton.setOnClickListener {
            if (settingsBar.visibility == android.view.View.VISIBLE) {
                settingsBar.visibility = android.view.View.GONE
            } else {
                updateSettingsBar()
                settingsBar.visibility = android.view.View.VISIBLE
            }
        }

        // 关闭设置栏按钮
        btnCloseSettings.setOnClickListener {
            settingsBar.visibility = android.view.View.GONE
        }

        // 点击设置栏跳转到设置页面
        settingsBar.setOnClickListener {
            val intent = Intent(this, ChatSettingsActivity::class.java)
            startActivity(intent)
        }

        // 更新快捷设置显示
        updateQuickSettingsDisplay()

        // 显示欢迎消息
        if (savedInstanceState == null) {
            showWelcomeMessage()
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次返回时更新设置显示
        updateQuickSettingsDisplay()
        updateSettingsBar()
    }

    private fun sendMessage() {
        val message = inputText.text.toString().trim()
        if (message.isEmpty()) {
            Toast.makeText(this, "请输入问题", Toast.LENGTH_SHORT).show()
            return
        }

        // 获取 API Key
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            showApiKeyDialog()
            return
        }

        // 发送消息
        viewModel.askQuestion(message, this)

        // 清空输入框
        inputText.text.clear()

        // 隐藏键盘
        hideKeyboard()
    }

    private fun showWelcomeMessage() {
        // 可以添加欢迎消息
    }

    private fun showClearConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("清空对话")
            .setMessage("确定要清空所有对话记录吗？")
            .setPositiveButton("确定") { _, _ ->
                viewModel.clearConversation()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showApiKeyDialog() {
        val inputEditText = EditText(this).apply {
            hint = "请输入 OpenAI API Key"
            setText(getApiKey())
        }

        AlertDialog.Builder(this)
            .setTitle("配置 API Key")
            .setMessage("请输入您的 OpenAI API Key\n\n您可以在 https://platform.openai.com/api-keys 获取")
            .setView(inputEditText)
            .setPositiveButton("保存") { _, _ ->
                val apiKey = inputEditText.text.toString().trim()
                saveApiKey(apiKey)
                Toast.makeText(this, "API Key 已保存", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun getApiKey(): String {
        val prefs = getSharedPreferences("chat_settings", Context.MODE_PRIVATE)
        return prefs.getString("api_key", "") ?: ""
    }

    private fun saveApiKey(apiKey: String) {
        val prefs = getSharedPreferences("chat_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("api_key", apiKey).apply()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputText.windowToken, 0)
    }

    /**
     * 更新快捷设置按钮显示
     */
    private fun updateQuickSettingsDisplay() {
        val prefs = getSharedPreferences("chat_settings", Context.MODE_PRIVATE)
        val apiKey = prefs.getString("api_key", "") ?: ""
        val model = prefs.getString("model", "gpt-3.5-turbo") ?: "gpt-3.5-turbo"

        if (apiKey.isEmpty()) {
            tvQuickSettings.text = "⚠️ 请配置 API Key 和模型"
            tvQuickSettings.setTextColor(getColor(android.R.color.holo_red_dark))
        } else {
            // 显示模型简称
            val modelShort = when {
                model.contains("gpt-4o-mini") -> "GPT-4o-mini"
                model.contains("gpt-4o") -> "GPT-4o"
                model.contains("gpt-4-turbo") -> "GPT-4-turbo"
                model.contains("gpt-4") -> "GPT-4"
                model.contains("16k") -> "GPT-3.5-16k"
                else -> "GPT-3.5"
            }
            tvQuickSettings.text = "当前: $modelShort | 点击查看或修改配置"
            tvQuickSettings.setTextColor(getColor(android.R.color.darker_gray))
        }
    }

    /**
     * 更新设置栏详细信息
     */
    private fun updateSettingsBar() {
        val prefs = getSharedPreferences("chat_settings", Context.MODE_PRIVATE)
        val apiKey = prefs.getString("api_key", "") ?: ""
        val model = prefs.getString("model", "gpt-3.5-turbo") ?: "gpt-3.5-turbo"
        val temperature = prefs.getFloat("temperature", 0.7f)
        val maxTokens = prefs.getInt("max_tokens", 1000)

        // 显示模型
        tvCurrentModel.text = "模型: $model (T=$temperature, MaxTokens=$maxTokens)"

        // 显示 API Key 状态
        if (apiKey.isEmpty()) {
            tvApiKeyStatus.text = "API Key: ❌ 未配置"
            tvApiKeyStatus.setTextColor(getColor(android.R.color.holo_red_dark))
        } else {
            // 显示部分 API Key（保护隐私）
            val maskedKey = if (apiKey.length > 10) {
                "${apiKey.substring(0, 7)}...${apiKey.substring(apiKey.length - 4)}"
            } else {
                "sk-****"
            }
            tvApiKeyStatus.text = "API Key: ✅ $maskedKey"
            tvApiKeyStatus.setTextColor(getColor(android.R.color.holo_green_dark))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

package com.example.traditional_chinese_medicine_ai_proj

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

/**
 * AI 问答设置页面
 */
class ChatSettingsActivity : AppCompatActivity() {

    private lateinit var etApiKey: TextInputEditText
    private lateinit var spinnerModel: AutoCompleteTextView
    private lateinit var sliderTemperature: Slider
    private lateinit var sliderMaxTokens: Slider
    private lateinit var tvTemperatureValue: TextView
    private lateinit var tvMaxTokensValue: TextView
    private lateinit var btnSave: Button
    private lateinit var btnReset: Button

    companion object {
        // 默认值
        const val DEFAULT_MODEL = "gpt-3.5-turbo"
        const val DEFAULT_TEMPERATURE = 0.7f
        const val DEFAULT_MAX_TOKENS = 1000

        // 可选模型列表
        val AVAILABLE_MODELS = arrayOf(
            "gpt-3.5-turbo",
            "gpt-3.5-turbo-16k",
            "gpt-4",
            "gpt-4-turbo-preview",
            "gpt-4o",
            "gpt-4o-mini"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_settings)

        // 设置工具栏
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 初始化视图
        etApiKey = findViewById(R.id.etApiKey)
        spinnerModel = findViewById(R.id.spinnerModel)
        sliderTemperature = findViewById(R.id.sliderTemperature)
        sliderMaxTokens = findViewById(R.id.sliderMaxTokens)
        tvTemperatureValue = findViewById(R.id.tvTemperatureValue)
        tvMaxTokensValue = findViewById(R.id.tvMaxTokensValue)
        btnSave = findViewById(R.id.btnSave)
        btnReset = findViewById(R.id.btnReset)

        // 设置模型下拉列表
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, AVAILABLE_MODELS)
        spinnerModel.setAdapter(adapter)

        // 加载已保存的设置
        loadSettings()

        // 监听 Slider 变化
        sliderTemperature.addOnChangeListener { _, value, _ ->
            tvTemperatureValue.text = String.format("%.1f", value)
        }

        sliderMaxTokens.addOnChangeListener { _, value, _ ->
            tvMaxTokensValue.text = value.toInt().toString()
        }

        // 保存按钮
        btnSave.setOnClickListener {
            saveSettings()
        }

        // 重置按钮
        btnReset.setOnClickListener {
            resetToDefaults()
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("chat_settings", Context.MODE_PRIVATE)

        etApiKey.setText(prefs.getString("api_key", ""))
        spinnerModel.setText(prefs.getString("model", DEFAULT_MODEL), false)

        val temperature = prefs.getFloat("temperature", DEFAULT_TEMPERATURE)
        sliderTemperature.value = temperature
        tvTemperatureValue.text = String.format("%.1f", temperature)

        val maxTokens = prefs.getInt("max_tokens", DEFAULT_MAX_TOKENS)
        sliderMaxTokens.value = maxTokens.toFloat()
        tvMaxTokensValue.text = maxTokens.toString()
    }

    private fun saveSettings() {
        val apiKey = etApiKey.text.toString().trim()

        if (apiKey.isEmpty()) {
            Toast.makeText(this, "请输入 API Key", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("chat_settings", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("api_key", apiKey)
            putString("model", spinnerModel.text.toString())
            putFloat("temperature", sliderTemperature.value)
            putInt("max_tokens", sliderMaxTokens.value.toInt())
            apply()
        }

        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun resetToDefaults() {
        spinnerModel.setText(DEFAULT_MODEL, false)
        sliderTemperature.value = DEFAULT_TEMPERATURE
        sliderMaxTokens.value = DEFAULT_MAX_TOKENS.toFloat()

        Toast.makeText(this, "已恢复默认设置", Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

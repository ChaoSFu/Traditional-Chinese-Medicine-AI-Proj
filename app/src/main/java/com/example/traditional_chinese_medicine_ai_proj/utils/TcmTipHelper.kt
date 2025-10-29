package com.example.traditional_chinese_medicine_ai_proj.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 中医提示语助手
 * 提供各类任务完成后的正向反馈提示语
 */
object TcmTipHelper {

    /**
     * 提示语分类
     */
    object Category {
        const val HERB = "herb"                 // 服药
        const val MASSAGE = "massage"           // 按摩/穴位按压
        const val MOXIBUSTION = "moxibustion"   // 刮痧/艾灸
        const val DIET = "diet"                 // 食疗
        const val REST = "rest"                 // 作息/睡眠
        const val EXERCISE = "exercise"         // 运动
        const val RECORD = "record"             // 病情记录
        const val APPOINTMENT = "appointment"   // 预约挂号
        const val POINTS = "points"             // 积分兑换
        const val COMPLETE = "complete"         // 完成周期治疗
        const val DEFAULT = "default"           // 默认
    }

    /**
     * 提示语数据结构
     */
    data class TipCategory(
        val title: String,
        val tips: List<String>
    )

    private var tipsMap: Map<String, TipCategory>? = null

    /**
     * 加载提示语数据
     */
    private fun loadTips(context: Context): Map<String, TipCategory> {
        if (tipsMap != null) return tipsMap!!

        try {
            val json = context.assets.open("mock/tips.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<Map<String, TipCategory>>() {}.type
            tipsMap = Gson().fromJson(json, type)
            return tipsMap!!
        } catch (e: Exception) {
            e.printStackTrace()
            // 返回默认提示语
            return mapOf(
                Category.DEFAULT to TipCategory(
                    title = "🌿 健康养护",
                    tips = listOf("健康在积累中 🌿", "每一步都算数 ✨")
                )
            )
        }
    }

    /**
     * 获取随机提示语
     * @param context Context
     * @param category 提示语分类，使用 TcmTipHelper.Category 常量
     * @return 提示语文本
     */
    fun getTip(context: Context, category: String = Category.DEFAULT): String {
        val tips = loadTips(context)
        val tipCategory = tips[category] ?: tips[Category.DEFAULT]!!
        return tipCategory.tips.random()
    }

    /**
     * 获取提示语标题
     * @param context Context
     * @param category 提示语分类
     * @return 标题文本
     */
    fun getTitle(context: Context, category: String = Category.DEFAULT): String {
        val tips = loadTips(context)
        val tipCategory = tips[category] ?: tips[Category.DEFAULT]!!
        return tipCategory.title
    }

    /**
     * 获取整个提示语分类（包含标题和所有提示语）
     */
    fun getTipCategory(context: Context, category: String = Category.DEFAULT): TipCategory {
        val tips = loadTips(context)
        return tips[category] ?: tips[Category.DEFAULT]!!
    }
}

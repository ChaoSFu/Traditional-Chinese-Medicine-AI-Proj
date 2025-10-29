package com.example.traditional_chinese_medicine_ai_proj.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * 积分管理工具
 * 使用SharedPreferences存储积分信息
 */
object PointsManager {

    private const val PREFS_NAME = "tcm_user_points"
    private const val KEY_POINTS = "points"
    private const val KEY_LEVEL = "level"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 获取当前积分
     */
    fun getPoints(context: Context): Int {
        return getPrefs(context).getInt(KEY_POINTS, 350) // 默认350积分
    }

    /**
     * 添加积分
     */
    fun addPoints(context: Context, points: Int): Int {
        val prefs = getPrefs(context)
        val currentPoints = prefs.getInt(KEY_POINTS, 350)
        val newPoints = currentPoints + points

        prefs.edit().putInt(KEY_POINTS, newPoints).apply()

        // 更新等级
        updateLevel(context, newPoints)

        return newPoints
    }

    /**
     * 扣除积分
     */
    fun deductPoints(context: Context, points: Int): Boolean {
        val prefs = getPrefs(context)
        val currentPoints = prefs.getInt(KEY_POINTS, 350)

        if (currentPoints < points) {
            return false // 积分不足
        }

        val newPoints = currentPoints - points
        prefs.edit().putInt(KEY_POINTS, newPoints).apply()

        // 更新等级
        updateLevel(context, newPoints)

        return true
    }

    /**
     * 根据积分更新会员等级
     */
    private fun updateLevel(context: Context, points: Int) {
        val level = when {
            points >= 1000 -> "钻石会员"
            points >= 500 -> "金牌会员"
            points >= 200 -> "银牌会员"
            else -> "铜牌会员"
        }

        getPrefs(context).edit().putString(KEY_LEVEL, level).apply()
    }

    /**
     * 获取当前等级
     */
    fun getLevel(context: Context): String {
        val points = getPoints(context)
        return when {
            points >= 1000 -> "钻石会员"
            points >= 500 -> "金牌会员"
            points >= 200 -> "银牌会员"
            else -> "铜牌会员"
        }
    }

    /**
     * 重置积分（测试用）
     */
    fun resetPoints(context: Context) {
        getPrefs(context).edit()
            .putInt(KEY_POINTS, 350)
            .putString(KEY_LEVEL, "银牌会员")
            .apply()
    }
}

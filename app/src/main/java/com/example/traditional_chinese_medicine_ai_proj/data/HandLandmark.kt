package com.example.traditional_chinese_medicine_ai_proj.data

import android.graphics.PointF

/**
 * 手部关键点数据模型
 * Hand landmark data model
 */
data class HandLandmark(
    val x: Float,      // X坐标（归一化 0-1）
    val y: Float,      // Y坐标（归一化 0-1）
    val z: Float = 0f  // Z坐标（深度，可选）
) {
    fun toPointF(): PointF = PointF(x, y)
}

/**
 * 手部关键点集合（MediaPipe 提供21个关键点）
 * MediaPipe Hand Landmarks (21 points)
 *
 * 关键点索引：
 * 0: WRIST (手腕)
 * 1-4: THUMB (拇指：CMC, MCP, IP, TIP)
 * 5-8: INDEX_FINGER (食指：MCP, PIP, DIP, TIP)
 * 9-12: MIDDLE_FINGER (中指：MCP, PIP, DIP, TIP)
 * 13-16: RING_FINGER (无名指：MCP, PIP, DIP, TIP)
 * 17-20: PINKY (小指：MCP, PIP, DIP, TIP)
 */
data class HandLandmarks(
    val landmarks: List<HandLandmark>,
    val handedness: String = "Unknown"  // "Left" 或 "Right"
) {
    companion object {
        // 关键点索引常量
        const val WRIST = 0
        const val THUMB_CMC = 1
        const val THUMB_MCP = 2
        const val THUMB_IP = 3
        const val THUMB_TIP = 4
        const val INDEX_FINGER_MCP = 5
        const val INDEX_FINGER_PIP = 6
        const val INDEX_FINGER_DIP = 7
        const val INDEX_FINGER_TIP = 8
        const val MIDDLE_FINGER_MCP = 9
        const val MIDDLE_FINGER_PIP = 10
        const val MIDDLE_FINGER_DIP = 11
        const val MIDDLE_FINGER_TIP = 12
        const val RING_FINGER_MCP = 13
        const val RING_FINGER_PIP = 14
        const val RING_FINGER_DIP = 15
        const val RING_FINGER_TIP = 16
        const val PINKY_MCP = 17
        const val PINKY_PIP = 18
        const val PINKY_DIP = 19
        const val PINKY_TIP = 20
    }

    fun getLandmark(index: Int): HandLandmark? {
        return if (index in landmarks.indices) landmarks[index] else null
    }
}

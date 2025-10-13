package com.example.traditional_chinese_medicine_ai_proj.utils

import android.graphics.PointF
import com.example.traditional_chinese_medicine_ai_proj.data.HandLandmark
import com.example.traditional_chinese_medicine_ai_proj.data.HandLandmarks

/**
 * 坐标计算工具类
 * Coordinate calculation utilities for acupoint localization
 */
object CoordinateUtils {

    /**
     * 计算两个关键点的中点
     * Calculate midpoint between two landmarks
     */
    fun midpoint(a: HandLandmark, b: HandLandmark): PointF {
        return PointF(
            (a.x + b.x) / 2f,
            (a.y + b.y) / 2f
        )
    }

    /**
     * 在两个关键点之间进行线性插值
     * Linear interpolation between two landmarks
     * @param t 插值参数 (0-1)，0表示点a，1表示点b
     */
    fun interpolate(a: HandLandmark, b: HandLandmark, t: Float): PointF {
        return PointF(
            a.x + t * (b.x - a.x),
            a.y + t * (b.y - a.y)
        )
    }

    /**
     * 计算合谷穴（LI4）位置
     * Calculate Hegu (LI4) acupoint position
     *
     * 定位方法：第1、2掌骨之间的中点
     * Location: Midpoint between 1st and 2nd metacarpal bones
     *
     * 近似算法：
     * - 使用食指掌指关节(INDEX_MCP, index 5)
     * - 使用拇指指间关节(THUMB_IP, index 3)
     * - 取两者中点
     */
    fun calculateHegu(landmarks: HandLandmarks): PointF? {
        val indexMcp = landmarks.getLandmark(HandLandmarks.INDEX_FINGER_MCP) ?: return null
        val thumbIp = landmarks.getLandmark(HandLandmarks.THUMB_IP) ?: return null

        return midpoint(indexMcp, thumbIp)
    }

    /**
     * 计算劳宫穴（PC8）位置
     * Calculate Laogong (PC8) acupoint position
     *
     * 定位方法：掌心，第2、3掌骨之间，握拳时中指尖所指处
     * Location: Palm center, between 2nd and 3rd metacarpal, where middle finger tip points when making a fist
     *
     * 近似算法：
     * - 使用中指掌指关节(MIDDLE_MCP, index 9)
     * - 使用中指近端指间关节(MIDDLE_PIP, index 10)
     * - 在两点之间进行插值，取靠近掌心的位置（t=0.33）
     */
    fun calculateLaogong(landmarks: HandLandmarks): PointF? {
        val middleMcp = landmarks.getLandmark(HandLandmarks.MIDDLE_FINGER_MCP) ?: return null
        val middlePip = landmarks.getLandmark(HandLandmarks.MIDDLE_FINGER_PIP) ?: return null
        val wrist = landmarks.getLandmark(HandLandmarks.WRIST) ?: return null

        // 方法1：中指关节向手腕方向插值
        val basePoint = interpolate(middleMcp, wrist, 0.3f)

        // 方法2：考虑手掌宽度，使用食指和无名指的掌指关节作为参考
        val indexMcp = landmarks.getLandmark(HandLandmarks.INDEX_FINGER_MCP)
        val ringMcp = landmarks.getLandmark(HandLandmarks.RING_FINGER_MCP)

        return if (indexMcp != null && ringMcp != null) {
            // 综合考虑：在中指-手腕方向和食指-无名指中线之间取平均
            val centerLine = midpoint(indexMcp, ringMcp)
            PointF(
                (basePoint.x + centerLine.x) / 2f,
                (basePoint.y + centerLine.y) / 2f
            )
        } else {
            basePoint
        }
    }

    /**
     * 计算两点之间的欧几里得距离
     * Calculate Euclidean distance between two points
     */
    fun distance(a: PointF, b: PointF): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    /**
     * 将归一化坐标转换为屏幕坐标
     * Convert normalized coordinates to screen coordinates
     * @param normalized 归一化坐标 (0-1)
     * @param width 屏幕宽度（像素）
     * @param height 屏幕高度（像素）
     */
    fun normalizedToScreen(normalized: PointF, width: Int, height: Int): PointF {
        return PointF(
            normalized.x * width,
            normalized.y * height
        )
    }

    /**
     * 判断点是否在指定矩形区域内
     * Check if point is within specified rectangular area
     */
    fun isPointInRect(point: PointF, left: Float, top: Float, right: Float, bottom: Float): Boolean {
        return point.x >= left && point.x <= right && point.y >= top && point.y <= bottom
    }
}

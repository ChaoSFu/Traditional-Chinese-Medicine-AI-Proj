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
     * 在 PointF 和 HandLandmark 之间进行线性插值
     * Linear interpolation between PointF and HandLandmark
     * @param t 插值参数 (0-1)，0表示点a，1表示点b
     */
    fun interpolate(a: PointF, b: HandLandmark, t: Float): PointF {
        return PointF(
            a.x + t * (b.x - a.x),
            a.y + t * (b.y - a.y)
        )
    }

    /**
     * 计算合谷穴（LI4）位置 - 手背穴位
     * Calculate Hegu (LI4) acupoint position - Back of hand
     *
     * 定位方法：第1、2掌骨之间的中点（手背侧）
     * Location: Midpoint between 1st and 2nd metacarpal bones (dorsal side)
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
     * 计算阳溪穴（LI5）位置 - 手背穴位
     * Calculate Yangxi (LI5) acupoint position - Back of hand
     *
     * 定位方法：腕背横纹桡侧，手拇指向上翘起时，在拇短伸肌腱与拇长伸肌腱之间的凹陷中
     * Location: On the dorsal wrist crease, in the depression between tendons when thumb is extended upward
     *
     * 近似算法：
     * - 使用手腕(WRIST, index 0)
     * - 使用拇指掌指关节(THUMB_MCP, index 2)
     * - 在两者之间插值，靠近手腕位置
     */
    fun calculateYangxi(landmarks: HandLandmarks): PointF? {
        val wrist = landmarks.getLandmark(HandLandmarks.WRIST) ?: return null
        val thumbMcp = landmarks.getLandmark(HandLandmarks.THUMB_MCP) ?: return null

        // 在手腕和拇指掌指关节之间，偏向手腕（t=0.25）
        return interpolate(wrist, thumbMcp, 0.25f)
    }

    /**
     * 计算劳宫穴（PC8）位置 - 手心穴位
     * Calculate Laogong (PC8) acupoint position - Palm side
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
        val wrist = landmarks.getLandmark(HandLandmarks.WRIST) ?: return null

        // 方法1：中指掌指关节向手腕方向插值
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
     * 计算少府穴（HT8）位置 - 手心穴位
     * Calculate Shaofu (HT8) acupoint position - Palm side
     *
     * 定位方法：掌心，第4、5掌骨之间，握拳时小指尖所指处
     * Location: Palm center, between 4th and 5th metacarpal, where little finger tip points when making a fist
     *
     * 近似算法：
     * - 使用小指掌指关节(PINKY_MCP, index 17)
     * - 使用无名指掌指关节(RING_MCP, index 13)
     * - 使用手腕作为参考
     * - 在小指掌指关节向手腕方向插值
     */
    fun calculateShaofu(landmarks: HandLandmarks): PointF? {
        val pinkyMcp = landmarks.getLandmark(HandLandmarks.PINKY_MCP) ?: return null
        val ringMcp = landmarks.getLandmark(HandLandmarks.RING_FINGER_MCP) ?: return null
        val wrist = landmarks.getLandmark(HandLandmarks.WRIST) ?: return null

        // 小指和无名指掌指关节的中点
        val basePoint = midpoint(pinkyMcp, ringMcp)

        // 向手腕方向插值，取掌心位置
        return interpolate(basePoint, wrist, 0.25f)
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

    /**
     * 判断当前是手心还是手背
     * Determine if showing palm or back of hand
     *
     * 判断逻辑：
     * 1. 使用手指PIP关节和手腕的Z坐标差异判断
     * 2. 手背：手指关节比手腕更靠近摄像头（Z值更大）
     * 3. 手心：手指关节比手腕更远离摄像头（Z值更小）
     *
     * @return true = 手心（palm），false = 手背（back of hand）
     */
    fun isPalmFacing(landmarks: HandLandmarks): Boolean {
        // 获取手腕
        val wrist = landmarks.getLandmark(HandLandmarks.WRIST)

        // 获取手指的近端指间关节（PIP）- 这些点在手掌弯曲时最能体现手心/手背
        val indexPip = landmarks.getLandmark(HandLandmarks.INDEX_FINGER_PIP)
        val middlePip = landmarks.getLandmark(HandLandmarks.MIDDLE_FINGER_PIP)
        val ringPip = landmarks.getLandmark(HandLandmarks.RING_FINGER_PIP)
        val pinkyPip = landmarks.getLandmark(HandLandmarks.PINKY_PIP)

        // 如果关键点不完整，返回默认值（手背）
        if (wrist == null || indexPip == null || middlePip == null ||
            ringPip == null || pinkyPip == null) {
            return false
        }

        // 计算手指关节的平均Z坐标
        val fingersAvgZ = (indexPip.z + middlePip.z + ringPip.z + pinkyPip.z) / 4f

        // 计算Z坐标差异
        val zDiff = fingersAvgZ - wrist.z

        // 手心：手指关节的Z坐标小于手腕（负值，手指离摄像头更近）
        // 手背：手指关节的Z坐标大于手腕（正值，手指离摄像头更远）
        // 使用阈值避免边界情况
        return zDiff < -0.01f  // 手指关节比手腕更靠近摄像头 = 手心
    }

    /**
     * 获取手势描述文本
     * Get hand pose description text
     */
    fun getHandPoseDescription(landmarks: HandLandmarks): String {
        return if (isPalmFacing(landmarks)) {
            "手心"
        } else {
            "手背"
        }
    }
}

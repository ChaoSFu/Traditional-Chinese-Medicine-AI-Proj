package com.example.traditional_chinese_medicine_ai_proj.utils

import com.example.traditional_chinese_medicine_ai_proj.data.HandLandmark
import com.example.traditional_chinese_medicine_ai_proj.data.HandLandmarks
import java.util.LinkedList

/**
 * 关键点平滑滤波器
 * 使用移动平均和卡尔曼滤波混合方法来平滑关键点，减少抖动
 */
class LandmarkSmoother(
    private val windowSize: Int = 5,  // 移动平均窗口大小
    private val smoothingFactor: Float = 0.5f  // 平滑因子 (0-1)，越大越平滑但延迟越高
) {
    // 存储历史关键点数据
    private val landmarkHistory = LinkedList<List<HandLandmark>>()

    // 上一帧的平滑结果，用于指数移动平均
    private var previousSmoothed: List<HandLandmark>? = null

    // 运动检测相关
    private var movementHistory = LinkedList<Float>()  // 存储历史移动速度
    private var isStationary = false  // 是否处于静止状态
    private var stationaryFrameCount = 0  // 静止帧计数器

    companion object {
        // MediaPipe 手部关键点索引
        // 0: 手腕
        // 1-4: 大拇指 (1:掌指关节, 2:指间关节, 3:近指间关节, 4:指尖)
        // 5-8: 食指
        // 9-12: 中指
        // 13-16: 无名指
        // 17-20: 小指

        // 手指末端关键点（需要更强的平滑）
        private val FINGER_TIP_INDICES = setOf(4, 8, 12, 16, 20)

        // 手指关节（需要中等平滑）
        private val FINGER_JOINT_INDICES = setOf(2, 3, 6, 7, 10, 11, 14, 15, 18, 19)

        // 掌部关键点（稳定，需要较少平滑）
        private val PALM_INDICES = setOf(0, 1, 5, 9, 13, 17)

        // 运动检测阈值（优化后的参数）
        private const val STATIONARY_THRESHOLD = 0.005f  // 静止阈值（更敏感的静止判断）
        private const val STATIONARY_FRAMES_REQUIRED = 3  // 需要连续多少帧静止才认为真正静止（快速响应）
        private const val MOVEMENT_HISTORY_SIZE = 8  // 运动历史记录大小（减小以提高响应）

        // 平滑强度参数（简化版，单一alpha值）
        private const val SMOOTH_ALPHA = 0.7f  // 平滑系数（性能优先）
        private const val OUTLIER_THRESHOLD = 0.15f  // 异常点检测阈值

        // 死区机制：低于此阈值的微小变化将被忽略（减少高频抖动）
        private const val DEAD_ZONE = 0.005f  // 稍微放宽以提升性能

        // 帧间异常过滤：若关键点变化速度超过阈值则判定为异常
        private const val MAX_FRAME_VELOCITY = 0.08f  // 放宽到8%，减少误判
    }

    /**
     * 对手部关键点进行平滑处理
     */
    fun smooth(handLandmarks: HandLandmarks?): HandLandmarks? {
        if (handLandmarks == null) {
            reset()
            return null
        }

        val currentLandmarks = handLandmarks.landmarks

        // 添加到历史记录
        landmarkHistory.add(currentLandmarks)
        if (landmarkHistory.size > windowSize) {
            landmarkHistory.removeFirst()
        }

        // 如果历史数据不足，直接返回当前数据
        if (landmarkHistory.size < 2) {
            previousSmoothed = currentLandmarks
            return handLandmarks
        }

        // 帧间异常检测：若速度过快则回退到上一帧
        if (previousSmoothed != null) {
            val maxVelocity = calculateMaxVelocity(currentLandmarks, previousSmoothed!!)
            if (maxVelocity > MAX_FRAME_VELOCITY) {
                // 异常跳点，直接返回上一帧的结果
                return HandLandmarks(
                    landmarks = previousSmoothed!!,
                    handedness = handLandmarks.handedness
                )
            }
        }

        // 简化版平滑：直接应用EMA，无需复杂的运动状态检测
        val smoothed = if (previousSmoothed != null) {
            // 应用指数平滑
            applyExponentialSmoothing(currentLandmarks, previousSmoothed!!, SMOOTH_ALPHA)
        } else {
            currentLandmarks
        }

        previousSmoothed = smoothed

        return HandLandmarks(
            landmarks = smoothed,
            handedness = handLandmarks.handedness
        )
    }

    /**
     * 计算移动平均
     */
    private fun calculateMovingAverage(history: List<List<HandLandmark>>): List<HandLandmark> {
        val numLandmarks = history.first().size
        val result = mutableListOf<HandLandmark>()

        for (i in 0 until numLandmarks) {
            var sumX = 0f
            var sumY = 0f
            var sumZ = 0f

            // 加权平均，最近的帧权重更高
            var totalWeight = 0f
            history.forEachIndexed { index, landmarks ->
                val weight = (index + 1).toFloat() // 线性递增权重
                sumX += landmarks[i].x * weight
                sumY += landmarks[i].y * weight
                sumZ += landmarks[i].z * weight
                totalWeight += weight
            }

            result.add(
                HandLandmark(
                    x = sumX / totalWeight,
                    y = sumY / totalWeight,
                    z = sumZ / totalWeight
                )
            )
        }

        return result
    }

    /**
     * 应用指数移动平均 (EMA)
     * 针对不同类型的关键点使用不同的平滑强度
     */
    private fun applyExponentialSmoothing(
        current: List<HandLandmark>,
        previous: List<HandLandmark>,
        alpha: Float
    ): List<HandLandmark> {
        return current.mapIndexed { index, landmark ->
            val prev = previous[index]

            // 根据关键点类型调整平滑强度
            val adjustedAlpha = when {
                FINGER_TIP_INDICES.contains(index) -> {
                    // 指尖：中等平滑，保持一定响应速度
                    alpha * 0.8f
                }
                FINGER_JOINT_INDICES.contains(index) -> {
                    // 关节：较少平滑，提高响应速度
                    alpha * 0.9f
                }
                PALM_INDICES.contains(index) -> {
                    // 掌部：最少平滑，保持快速响应
                    alpha
                }
                else -> alpha
            }

            // 死区机制：微小变化直接使用上一帧的值
            val dx = kotlin.math.abs(landmark.x - prev.x)
            val dy = kotlin.math.abs(landmark.y - prev.y)
            val dz = kotlin.math.abs(landmark.z - prev.z)

            HandLandmark(
                x = if (dx < DEAD_ZONE) prev.x else adjustedAlpha * landmark.x + (1 - adjustedAlpha) * prev.x,
                y = if (dy < DEAD_ZONE) prev.y else adjustedAlpha * landmark.y + (1 - adjustedAlpha) * prev.y,
                z = if (dz < DEAD_ZONE) prev.z else adjustedAlpha * landmark.z + (1 - adjustedAlpha) * prev.z
            )
        }
    }

    /**
     * 检测并过滤异常跳变
     * 如果某个关键点突然移动太大，认为是误检测，使用历史数据
     */
    private fun filterOutliers(
        current: List<HandLandmark>,
        previous: List<HandLandmark>
    ): List<HandLandmark> {
        val threshold = OUTLIER_THRESHOLD  // 使用统一的异常点阈值

        return current.mapIndexed { index, landmark ->
            val prev = previous[index]

            // 计算移动距离
            val distance = kotlin.math.sqrt(
                (landmark.x - prev.x) * (landmark.x - prev.x) +
                (landmark.y - prev.y) * (landmark.y - prev.y) +
                (landmark.z - prev.z) * (landmark.z - prev.z)
            )

            // 如果移动过大，使用插值而不是直接采用新值
            if (distance > threshold) {
                // 限制移动距离
                val ratio = threshold / distance
                HandLandmark(
                    x = prev.x + (landmark.x - prev.x) * ratio,
                    y = prev.y + (landmark.y - prev.y) * ratio,
                    z = prev.z + (landmark.z - prev.z) * ratio
                )
            } else {
                landmark
            }
        }
    }

    /**
     * 计算最大移动速度（用于异常检测）
     */
    private fun calculateMaxVelocity(
        current: List<HandLandmark>,
        previous: List<HandLandmark>
    ): Float {
        var maxDistance = 0f
        current.forEachIndexed { index, landmark ->
            val prev = previous[index]
            val distance = kotlin.math.sqrt(
                (landmark.x - prev.x) * (landmark.x - prev.x) +
                (landmark.y - prev.y) * (landmark.y - prev.y)
            )
            if (distance > maxDistance) {
                maxDistance = distance
            }
        }
        return maxDistance
    }

    /**
     * 计算平均移动速度
     */
    private fun calculateMovementSpeed(
        current: List<HandLandmark>,
        previous: List<HandLandmark>
    ): Float {
        var totalDistance = 0f
        current.forEachIndexed { index, landmark ->
            val prev = previous[index]
            val distance = kotlin.math.sqrt(
                (landmark.x - prev.x) * (landmark.x - prev.x) +
                (landmark.y - prev.y) * (landmark.y - prev.y) +
                (landmark.z - prev.z) * (landmark.z - prev.z)
            )
            totalDistance += distance
        }
        return totalDistance / current.size
    }

    /**
     * 更新运动状态
     */
    private fun updateMovementState(speed: Float) {
        // 添加到历史记录
        movementHistory.add(speed)
        if (movementHistory.size > MOVEMENT_HISTORY_SIZE) {
            movementHistory.removeFirst()
        }

        // 计算平均速度
        val avgSpeed = if (movementHistory.isNotEmpty()) {
            movementHistory.average().toFloat()
        } else {
            speed
        }

        // 判断是否静止
        if (avgSpeed < STATIONARY_THRESHOLD) {
            stationaryFrameCount++
            if (stationaryFrameCount >= STATIONARY_FRAMES_REQUIRED) {
                isStationary = true
            }
        } else {
            stationaryFrameCount = 0
            isStationary = false
        }
    }

    /**
     * 处理静止状态
     * 使用强平滑，提高精度
     */
    private fun processStationaryState(currentLandmarks: List<HandLandmark>): List<HandLandmark> {
        // 添加到历史记录
        landmarkHistory.add(currentLandmarks)
        if (landmarkHistory.size > windowSize * 2) {  // 静止时使用更大的窗口
            landmarkHistory.removeFirst()
        }

        // 使用更多历史数据进行平滑
        val averaged = calculateMovingAverage(landmarkHistory)

        // 应用强平滑
        return if (previousSmoothed != null) {
            applyExponentialSmoothing(
                averaged,
                previousSmoothed!!,
                0.1f  // 静止时使用很小的 alpha，强力平滑
            )
        } else {
            averaged
        }
    }

    /**
     * 处理运动状态
     * 使用轻平滑，快速响应
     */
    private fun processMovingState(currentLandmarks: List<HandLandmark>): List<HandLandmark> {
        // 添加到历史记录
        landmarkHistory.add(currentLandmarks)
        if (landmarkHistory.size > 3) {  // 运动时只保留少量历史
            landmarkHistory.removeFirst()
        }

        // 过滤异常跳变
        val filtered = if (previousSmoothed != null) {
            filterOutliers(currentLandmarks, previousSmoothed!!)
        } else {
            currentLandmarks
        }

        // 更新历史记录
        landmarkHistory[landmarkHistory.size - 1] = filtered

        // 使用少量历史数据
        val averaged = if (landmarkHistory.size >= 2) {
            calculateMovingAverage(landmarkHistory)
        } else {
            filtered
        }

        // 应用轻平滑
        return if (previousSmoothed != null) {
            applyExponentialSmoothing(
                averaged,
                previousSmoothed!!,
                SMOOTH_ALPHA  // 运动时使用大的 alpha，快速响应
            )
        } else {
            averaged
        }
    }

    /**
     * 获取当前运动状态
     * @return true = 静止，false = 运动
     */
    fun getIsStationary(): Boolean = isStationary

    /**
     * 重置滤波器状态
     */
    fun reset() {
        landmarkHistory.clear()
        previousSmoothed = null
        movementHistory.clear()
        isStationary = false
        stationaryFrameCount = 0
    }
}

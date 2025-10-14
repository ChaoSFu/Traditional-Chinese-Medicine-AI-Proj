package com.example.traditional_chinese_medicine_ai_proj.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.traditional_chinese_medicine_ai_proj.data.HandLandmark
import com.example.traditional_chinese_medicine_ai_proj.data.HandLandmarks
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

/**
 * MediaPipe Hand Landmarker 封装类
 * Wrapper class for MediaPipe Hand Landmarker
 */
class HandLandmarkerHelper(
    private val context: Context,
    private val listener: HandLandmarkerListener? = null,
    private val minDetectionConfidence: Float = 0.7f,  // 提高检测置信度，减少误识别
    private val minTrackingConfidence: Float = 0.8f,   // 提高跟踪置信度，增强稳定性
    private val minPresenceConfidence: Float = 0.7f    // 提高存在置信度，减少点跳动
) {
    private var handLandmarker: HandLandmarker? = null
    private var isInitialized = false

    companion object {
        private const val TAG = "HandLandmarkerHelper"
        // MediaPipe 要求路径必须包含斜杠，使用相对于 assets 的路径
        private const val MODEL_ASSET_PATH = "hand_landmarker.task"
        private const val MAX_NUM_HANDS = 1
    }

    /**
     * 结果监听器接口
     */
    interface HandLandmarkerListener {
        fun onResults(result: HandLandmarks?)
        fun onError(error: String)
    }

    /**
     * 初始化 HandLandmarker
     */
    fun initialize(): Boolean {
        // 检查模型文件是否存在
        try {
            context.assets.open(MODEL_ASSET_PATH).close()
            Log.d(TAG, "Model file found: $MODEL_ASSET_PATH")
        } catch (e: Exception) {
            Log.e(TAG, "Model file not found in assets: $MODEL_ASSET_PATH", e)
            listener?.onError("模型文件未找到，请确保已下载 hand_landmarker.task")
            return false
        }

        // 先尝试 GPU，如果失败则使用 CPU
        val delegates = listOf(Delegate.GPU, Delegate.CPU)

        for (delegate in delegates) {
            try {
                Log.d(TAG, "Attempting to initialize with delegate: $delegate")

                val baseOptions = BaseOptions.builder()
                    .setDelegate(delegate)
                    .setModelAssetPath(MODEL_ASSET_PATH)
                    .build()

                val options = HandLandmarker.HandLandmarkerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setRunningMode(RunningMode.VIDEO)  // VIDEO模式：利用时序信息，关键点更连续稳定
                    .setNumHands(MAX_NUM_HANDS)
                    .setMinHandDetectionConfidence(minDetectionConfidence)
                    .setMinTrackingConfidence(minTrackingConfidence)
                    .setMinHandPresenceConfidence(minPresenceConfidence)
                    .build()

                handLandmarker = HandLandmarker.createFromOptions(context, options)
                isInitialized = true
                Log.d(TAG, "HandLandmarker initialized successfully with $delegate")
                return true
            } catch (e: Exception) {
                Log.w(TAG, "Failed to initialize with $delegate: ${e.message}")
                if (delegate == Delegate.CPU) {
                    // CPU 也失败了，记录错误
                    Log.e(TAG, "Error initializing HandLandmarker with all delegates", e)
                    listener?.onError("初始化失败: ${e.message}")
                    return false
                }
                // 继续尝试下一个 delegate
            }
        }

        return false
    }

    /**
     * 检测手部关键点
     * @param bitmap 输入图像
     * @param frameTimestampMs 帧时间戳（毫秒），VIDEO模式必需
     * @return 检测结果
     */
    fun detect(bitmap: Bitmap, frameTimestampMs: Long = System.currentTimeMillis()): HandLandmarks? {
        if (!isInitialized || handLandmarker == null) {
            Log.w(TAG, "HandLandmarker not initialized")
            return null
        }

        return try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = handLandmarker?.detectForVideo(mpImage, frameTimestampMs)
            parseResult(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error during detection", e)
            listener?.onError("检测失败: ${e.message}")
            null
        }
    }

    /**
     * 异步检测（用于视频流）
     * @param bitmap 输入图像（方法内部会负责回收）
     * @param frameTimestampMs 帧时间戳（毫秒），VIDEO模式必需
     */
    fun detectAsync(bitmap: Bitmap, frameTimestampMs: Long = System.currentTimeMillis()) {
        if (!isInitialized || handLandmarker == null) {
            Log.w(TAG, "HandLandmarker not initialized")
            bitmap.recycle()
            return
        }

        if (bitmap.isRecycled) {
            Log.w(TAG, "Bitmap is already recycled")
            return
        }

        try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = handLandmarker?.detectForVideo(mpImage, frameTimestampMs)
            val handLandmarks = parseResult(result)
            listener?.onResults(handLandmarks)
        } catch (e: Exception) {
            Log.e(TAG, "Error during async detection", e)
            listener?.onError("检测失败: ${e.message}")
        } finally {
            // 检测完成后回收 Bitmap
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }

    /**
     * 解析 MediaPipe 结果
     */
    private fun parseResult(result: HandLandmarkerResult?): HandLandmarks? {
        if (result == null) return null

        val landmarks = result.landmarks()
        if (landmarks.isEmpty()) {
            return null
        }

        // 获取第一只手的关键点
        val firstHand = landmarks[0]
        val handLandmarkList = firstHand.map { landmark ->
            HandLandmark(
                x = landmark.x(),
                y = landmark.y(),
                z = landmark.z()
            )
        }

        // 获取手部方向（左手/右手）
        val handedness = if (result.handedness().isNotEmpty()) {
            val category = result.handedness()[0]
            if (category.isNotEmpty()) {
                category[0].categoryName() // "Left" 或 "Right"
            } else {
                "Unknown"
            }
        } else {
            "Unknown"
        }

        return HandLandmarks(
            landmarks = handLandmarkList,
            handedness = handedness
        )
    }

    /**
     * 释放资源
     */
    fun close() {
        handLandmarker?.close()
        handLandmarker = null
        isInitialized = false
        Log.d(TAG, "HandLandmarker closed")
    }

    /**
     * 检查是否已初始化
     */
    fun isReady(): Boolean = isInitialized && handLandmarker != null
}

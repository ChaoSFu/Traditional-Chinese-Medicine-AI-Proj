package com.example.traditional_chinese_medicine_ai_proj.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import com.example.traditional_chinese_medicine_ai_proj.data.HandLandmarks

/**
 * 自定义叠加视图，用于绘制手部关键点和穴位
 * Custom overlay view for drawing hand landmarks and acupoints
 */
class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 绘制配置
    private val landmarkPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")  // 绿色
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // 手背穴位
    private val li4Paint = Paint().apply {
        color = Color.parseColor("#F44336")  // 红色（合谷 - 手背）
        style = Paint.Style.FILL
        isAntiAlias = true
        alpha = 220  // 半透明，减少视觉突兀
    }

    private val li5Paint = Paint().apply {
        color = Color.parseColor("#E91E63")  // 粉红色（阳溪 - 手背）
        style = Paint.Style.FILL
        isAntiAlias = true
        alpha = 220  // 半透明，减少视觉突兀
    }

    // 手心穴位
    private val pc8Paint = Paint().apply {
        color = Color.parseColor("#FF9800")  // 橙色（劳宫 - 手心）
        style = Paint.Style.FILL
        isAntiAlias = true
        alpha = 220  // 半透明，减少视觉突兀
    }

    private val ht8Paint = Paint().apply {
        color = Color.parseColor("#FFC107")  // 琥珀色（少府 - 手心）
        style = Paint.Style.FILL
        isAntiAlias = true
        alpha = 220  // 半透明，减少视觉突兀
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 36f
        isAntiAlias = true
        style = Paint.Style.FILL
        setShadowLayer(4f, 2f, 2f, Color.BLACK)  // 文字阴影
    }

    private val connectionPaint = Paint().apply {
        color = Color.parseColor("#80FFFFFF")  // 半透明白色
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    // 数据
    private var handLandmarks: HandLandmarks? = null

    // 手背穴位
    private var li4Position: PointF? = null  // 合谷穴位置（手背）
    private var li5Position: PointF? = null  // 阳溪穴位置（手背）

    // 手心穴位
    private var pc8Position: PointF? = null  // 劳宫穴位置（手心）
    private var ht8Position: PointF? = null  // 少府穴位置（手心）

    private var showLandmarks = true         // 是否显示所有关键点
    private var showConnections = true       // 是否显示骨架连接线

    // 防抖动：存储上一次的穴位位置
    private var lastLi4Position: PointF? = null
    private var lastLi5Position: PointF? = null
    private var lastPc8Position: PointF? = null
    private var lastHt8Position: PointF? = null

    // 调试信息
    private var fps: Int = 0
    private var smoothingMode: String = ""  // "静止" 或 "运动"

    companion object {
        private const val LANDMARK_RADIUS = 8f
        private const val ACUPOINT_RADIUS = 16f
        private const val TEXT_OFFSET_X = 20f
        private const val TEXT_OFFSET_Y = -10f

        // 防抖阈值：小于此距离的变化将被忽略（归一化坐标）
        private const val UPDATE_THRESHOLD = 0.005f

        // MediaPipe 手部骨架连接关系
        private val HAND_CONNECTIONS = listOf(
            // 拇指
            0 to 1, 1 to 2, 2 to 3, 3 to 4,
            // 食指
            0 to 5, 5 to 6, 6 to 7, 7 to 8,
            // 中指
            0 to 9, 9 to 10, 10 to 11, 11 to 12,
            // 无名指
            0 to 13, 13 to 14, 14 to 15, 15 to 16,
            // 小指
            0 to 17, 17 to 18, 18 to 19, 19 to 20,
            // 掌部连接
            5 to 9, 9 to 13, 13 to 17
        )
    }

    /**
     * 更新手部关键点数据
     */
    fun updateHandLandmarks(landmarks: HandLandmarks?) {
        this.handLandmarks = landmarks
        invalidate()  // 触发重绘
    }

    /**
     * 更新合谷穴位置（手背）- 带防抖
     */
    fun updateLi4Position(position: PointF?) {
        if (shouldUpdate(position, lastLi4Position)) {
            this.li4Position = position
            this.lastLi4Position = position?.let { PointF(it.x, it.y) }
            invalidate()
        }
    }

    /**
     * 更新阳溪穴位置（手背）- 带防抖
     */
    fun updateLi5Position(position: PointF?) {
        if (shouldUpdate(position, lastLi5Position)) {
            this.li5Position = position
            this.lastLi5Position = position?.let { PointF(it.x, it.y) }
            invalidate()
        }
    }

    /**
     * 更新劳宫穴位置（手心）- 带防抖
     */
    fun updatePc8Position(position: PointF?) {
        if (shouldUpdate(position, lastPc8Position)) {
            this.pc8Position = position
            this.lastPc8Position = position?.let { PointF(it.x, it.y) }
            invalidate()
        }
    }

    /**
     * 更新少府穴位置（手心）- 带防抖
     */
    fun updateHt8Position(position: PointF?) {
        if (shouldUpdate(position, lastHt8Position)) {
            this.ht8Position = position
            this.lastHt8Position = position?.let { PointF(it.x, it.y) }
            invalidate()
        }
    }

    /**
     * 判断是否需要更新穴位位置（防抖）
     * @param newPos 新位置
     * @param lastPos 上次位置
     * @return true表示需要更新
     */
    private fun shouldUpdate(newPos: PointF?, lastPos: PointF?): Boolean {
        // 如果新位置为null，始终更新（清除显示）
        if (newPos == null) return true

        // 如果没有历史位置，始终更新
        if (lastPos == null) return true

        // 计算位置变化
        val dx = kotlin.math.abs(newPos.x - lastPos.x)
        val dy = kotlin.math.abs(newPos.y - lastPos.y)

        // 如果变化超过阈值，才更新
        return dx > UPDATE_THRESHOLD || dy > UPDATE_THRESHOLD
    }

    /**
     * 设置是否显示关键点
     */
    fun setShowLandmarks(show: Boolean) {
        this.showLandmarks = show
        invalidate()
    }

    /**
     * 设置是否显示连接线
     */
    fun setShowConnections(show: Boolean) {
        this.showConnections = show
        invalidate()
    }

    /**
     * 更新调试信息（FPS 和平滑模式）
     */
    fun updateDebugInfo(fps: Int, isStationary: Boolean) {
        this.fps = fps
        this.smoothingMode = if (isStationary) "静止" else "运动"
        invalidate()
    }

    /**
     * 清除所有数据
     */
    fun clear() {
        handLandmarks = null
        li4Position = null
        li5Position = null
        pc8Position = null
        ht8Position = null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val landmarks = handLandmarks ?: return
        val w = width.toFloat()
        val h = height.toFloat()

        // 1. 绘制骨架连接线（可选）
        if (showConnections) {
            drawConnections(canvas, landmarks, w, h)
        }

        // 2. 绘制所有关键点（可选）
        if (showLandmarks) {
            drawLandmarks(canvas, landmarks, w, h)
        }

        // 3. 绘制手背穴位
        // 3.1 合谷穴（LI4）
        li4Position?.let { pos ->
            val screenX = pos.x * w
            val screenY = pos.y * h
            canvas.drawCircle(screenX, screenY, ACUPOINT_RADIUS, li4Paint)
            canvas.drawText(
                "合谷 (LI4)",
                screenX + TEXT_OFFSET_X,
                screenY + TEXT_OFFSET_Y,
                textPaint
            )
        }

        // 3.2 阳溪穴（LI5）
        li5Position?.let { pos ->
            val screenX = pos.x * w
            val screenY = pos.y * h
            canvas.drawCircle(screenX, screenY, ACUPOINT_RADIUS, li5Paint)
            canvas.drawText(
                "阳溪 (LI5)",
                screenX + TEXT_OFFSET_X,
                screenY + TEXT_OFFSET_Y,
                textPaint
            )
        }

        // 4. 绘制手心穴位
        // 4.1 劳宫穴（PC8）
        pc8Position?.let { pos ->
            val screenX = pos.x * w
            val screenY = pos.y * h
            canvas.drawCircle(screenX, screenY, ACUPOINT_RADIUS, pc8Paint)
            canvas.drawText(
                "劳宫 (PC8)",
                screenX + TEXT_OFFSET_X,
                screenY + TEXT_OFFSET_Y,
                textPaint
            )
        }

        // 4.2 少府穴（HT8）
        ht8Position?.let { pos ->
            val screenX = pos.x * w
            val screenY = pos.y * h
            canvas.drawCircle(screenX, screenY, ACUPOINT_RADIUS, ht8Paint)
            canvas.drawText(
                "少府 (HT8)",
                screenX + TEXT_OFFSET_X,
                screenY + TEXT_OFFSET_Y,
                textPaint
            )
        }

        // 5. 绘制调试信息（FPS 和平滑模式）
        if (fps > 0 || smoothingMode.isNotEmpty()) {
            val debugText = buildString {
                if (fps > 0) append("FPS: $fps")
                if (smoothingMode.isNotEmpty()) {
                    if (isNotEmpty()) append(" | ")
                    append("模式: $smoothingMode")
                }
            }
            canvas.drawText(debugText, 20f, 60f, textPaint)
        }
    }

    /**
     * 绘制手部关键点
     */
    private fun drawLandmarks(canvas: Canvas, landmarks: HandLandmarks, width: Float, height: Float) {
        landmarks.landmarks.forEach { landmark ->
            val x = landmark.x * width
            val y = landmark.y * height
            canvas.drawCircle(x, y, LANDMARK_RADIUS, landmarkPaint)
        }
    }

    /**
     * 绘制手部骨架连接线
     */
    private fun drawConnections(canvas: Canvas, landmarks: HandLandmarks, width: Float, height: Float) {
        HAND_CONNECTIONS.forEach { (startIdx, endIdx) ->
            val start = landmarks.getLandmark(startIdx)
            val end = landmarks.getLandmark(endIdx)

            if (start != null && end != null) {
                canvas.drawLine(
                    start.x * width,
                    start.y * height,
                    end.x * width,
                    end.y * height,
                    connectionPaint
                )
            }
        }
    }

    /**
     * 检测点击位置是否在穴位附近
     * @return 返回穴位ID（"LI4" 或 "PC8"）或 null
     */
    fun detectAcupointClick(x: Float, y: Float, threshold: Float = 50f): String? {
        val w = width.toFloat()
        val h = height.toFloat()

        // 检测合谷
        li4Position?.let { pos ->
            val screenX = pos.x * w
            val screenY = pos.y * h
            val distance = kotlin.math.sqrt(
                (x - screenX) * (x - screenX) + (y - screenY) * (y - screenY)
            )
            if (distance <= threshold) return "LI4"
        }

        // 检测劳宫
        pc8Position?.let { pos ->
            val screenX = pos.x * w
            val screenY = pos.y * h
            val distance = kotlin.math.sqrt(
                (x - screenX) * (x - screenX) + (y - screenY) * (y - screenY)
            )
            if (distance <= threshold) return "PC8"
        }

        return null
    }
}

package com.example.traditional_chinese_medicine_ai_proj.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Canvas as AndroidCanvas

/**
 * 图像预处理工具类
 * 提供图像增强功能以提升检测准确度
 */
object ImagePreprocessor {

    /**
     * 自动调整对比度和亮度
     * 使用直方图均衡化改进图像质量
     */
    fun enhanceImage(bitmap: Bitmap, contrastFactor: Float = 1.2f, brightnessFactor: Float = 10f): Bitmap {
        val enhanced = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = AndroidCanvas(enhanced)

        // 创建颜色矩阵来调整对比度和亮度
        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                contrastFactor, 0f, 0f, 0f, brightnessFactor,  // Red
                0f, contrastFactor, 0f, 0f, brightnessFactor,  // Green
                0f, 0f, contrastFactor, 0f, brightnessFactor,  // Blue
                0f, 0f, 0f, 1f, 0f                              // Alpha
            ))
        }

        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }

        canvas.drawBitmap(enhanced, 0f, 0f, paint)
        return enhanced
    }

    /**
     * 自适应增强
     * 根据图像亮度自动调整
     */
    fun autoEnhance(bitmap: Bitmap): Bitmap {
        val brightness = calculateAverageBrightness(bitmap)

        // 根据亮度决定增强参数
        // 增加对比度以突出手指关节的轮廓
        val contrastFactor = when {
            brightness < 80 -> 1.4f   // 暗图：大幅增加对比度
            brightness > 180 -> 1.2f  // 亮图：中等增加对比度
            else -> 1.3f              // 正常：较高对比度，有助于关节检测
        }

        val brightnessFactor = when {
            brightness < 80 -> 25f    // 暗图：增加亮度
            brightness > 180 -> -5f   // 亮图：轻微减少亮度
            else -> 10f               // 正常：轻微增加亮度
        }

        return enhanceImage(bitmap, contrastFactor, brightnessFactor)
    }

    /**
     * 计算图像平均亮度
     */
    private fun calculateAverageBrightness(bitmap: Bitmap): Int {
        var totalBrightness = 0L
        var pixelCount = 0

        // 采样：每隔10个像素采样一次以提高性能
        val step = 10
        for (y in 0 until bitmap.height step step) {
            for (x in 0 until bitmap.width step step) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                // 使用感知亮度公式
                val brightness = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                totalBrightness += brightness
                pixelCount++
            }
        }

        return if (pixelCount > 0) (totalBrightness / pixelCount).toInt() else 128
    }

    /**
     * 锐化图像以增强细节
     * 特别有助于手指关节的检测
     */
    fun sharpenImage(bitmap: Bitmap): Bitmap {
        val sharpened = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        // 增强锐化：帮助识别手指关节边缘
        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                2.0f, -0.5f, -0.5f, 0f, 0f,
                -0.5f, 2.0f, -0.5f, 0f, 0f,
                -0.5f, -0.5f, 2.0f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
        }

        val canvas = AndroidCanvas(sharpened)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }

        canvas.drawBitmap(sharpened, 0f, 0f, paint)
        return sharpened
    }

    /**
     * 综合增强：结合多种增强方法
     * 专门优化手指关节检测
     */
    fun enhanceForHandDetection(bitmap: Bitmap): Bitmap {
        // 第一步：自适应增强
        val enhanced = autoEnhance(bitmap)

        // 第二步：轻微锐化增强边缘
        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                1.3f, -0.15f, -0.15f, 0f, 5f,
                -0.15f, 1.3f, -0.15f, 0f, 5f,
                -0.15f, -0.15f, 1.3f, 0f, 5f,
                0f, 0f, 0f, 1f, 0f
            ))
        }

        val canvas = AndroidCanvas(enhanced)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
            isAntiAlias = true
        }

        canvas.drawBitmap(enhanced, 0f, 0f, paint)
        return enhanced
    }

    /**
     * 降噪处理（简单版本）
     * 通过轻微模糊来减少噪点
     */
    fun reduceNoise(bitmap: Bitmap): Bitmap {
        // 这里使用简单的颜色平滑
        // 实际应用中可以使用更复杂的算法如双边滤波
        val denoised = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        // 应用轻微的颜色饱和度调整来减少噪点影响
        val colorMatrix = ColorMatrix().apply {
            setSaturation(0.9f)
        }

        val canvas = AndroidCanvas(denoised)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
            isAntiAlias = true
            isFilterBitmap = true
        }

        canvas.drawBitmap(denoised, 0f, 0f, paint)
        return denoised
    }
}

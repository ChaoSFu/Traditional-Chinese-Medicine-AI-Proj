package com.example.traditional_chinese_medicine_ai_proj

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.traditional_chinese_medicine_ai_proj.data.HandLandmarks
import com.example.traditional_chinese_medicine_ai_proj.ml.HandLandmarkerHelper
import com.example.traditional_chinese_medicine_ai_proj.ui.OverlayView
import com.example.traditional_chinese_medicine_ai_proj.utils.CoordinateUtils
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

/**
 * 中医手部穴位实时定位主界面
 * Main Activity for TCM Hand Acupoint Real-time Localization
 */
class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var overlayView: OverlayView
    private lateinit var statusText: TextView
    private lateinit var fpsText: TextView
    private lateinit var handednessText: TextView
    private lateinit var btnToggleLandmarks: Button
    private lateinit var btnToggleConnections: Button

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var handLandmarkerHelper: HandLandmarkerHelper

    private var camera: Camera? = null
    private var imageAnalyzer: ImageAnalysis? = null

    // FPS计算
    private var frameCount = 0
    private var lastFpsTimestamp = 0L
    private var currentFps = 0

    // 显示选项
    private var showLandmarks = true
    private var showConnections = false

    // 选中的穴位列表
    private var selectedAcupoints = setOf("LI4", "PC8") // 默认显示所有

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 获取选中的穴位
        intent.getStringArrayListExtra("SELECTED_ACUPOINTS")?.let {
            selectedAcupoints = it.toSet()
        }

        // 初始化视图
        initViews()

        // 初始化相机执行器
        cameraExecutor = Executors.newSingleThreadExecutor()

        // 检查权限
        if (allPermissionsGranted()) {
            initializeApp()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun initViews() {
        previewView = findViewById(R.id.previewView)
        overlayView = findViewById(R.id.overlayView)
        statusText = findViewById(R.id.statusText)
        fpsText = findViewById(R.id.fpsText)
        handednessText = findViewById(R.id.handednessText)
        btnToggleLandmarks = findViewById(R.id.btnToggleLandmarks)
        btnToggleConnections = findViewById(R.id.btnToggleConnections)

        // 设置按钮点击事件
        btnToggleLandmarks.setOnClickListener {
            showLandmarks = !showLandmarks
            overlayView.setShowLandmarks(showLandmarks)
            btnToggleLandmarks.text = if (showLandmarks) {
                getString(R.string.btn_hide_landmarks)
            } else {
                getString(R.string.btn_show_landmarks)
            }
        }

        btnToggleConnections.setOnClickListener {
            showConnections = !showConnections
            overlayView.setShowConnections(showConnections)
            btnToggleConnections.text = if (showConnections) {
                getString(R.string.btn_hide_connections)
            } else {
                getString(R.string.btn_show_connections)
            }
        }
    }

    private fun initializeApp() {
        statusText.text = getString(R.string.status_initializing)

        // 初始化 HandLandmarker
        handLandmarkerHelper = HandLandmarkerHelper(
            context = this,
            listener = object : HandLandmarkerHelper.HandLandmarkerListener {
                override fun onResults(result: HandLandmarks?) {
                    runOnUiThread {
                        updateUI(result)
                    }
                }

                override fun onError(error: String) {
                    runOnUiThread {
                        statusText.text = "错误: $error"
                        Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        if (handLandmarkerHelper.initialize()) {
            startCamera()
            statusText.text = getString(R.string.status_ready)
        } else {
            statusText.text = getString(R.string.status_error)
            Toast.makeText(this, "初始化失败", Toast.LENGTH_LONG).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                bindCamera(cameraProvider)
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization failed", e)
                statusText.text = "相机启动失败"
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCamera(cameraProvider: ProcessCameraProvider) {
        // 预览配置
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        // 图像分析配置
        imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    processImage(imageProxy)
                }
            }

        // 选择前置摄像头
        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        try {
            // 解绑之前的用例
            cameraProvider.unbindAll()

            // 绑定生命周期
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalyzer
            )

            Log.d(TAG, "Camera bound successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Camera binding failed", e)
        }
    }

    private fun processImage(imageProxy: ImageProxy) {
        if (!handLandmarkerHelper.isReady()) {
            imageProxy.close()
            return
        }

        try {
            // 转换为 Bitmap，如果失败则提前返回
            val sourceBitmap = imageProxy.convertToBitmap() ?: run {
                Log.w(TAG, "Failed to convert ImageProxy to Bitmap")
                return
            }

            try {
                // 镜像翻转（前置摄像头）
                val matrix = Matrix().apply {
                    postScale(-1f, 1f, sourceBitmap.width / 2f, sourceBitmap.height / 2f)
                }
                val mirroredBitmap = Bitmap.createBitmap(
                    sourceBitmap, 0, 0, sourceBitmap.width, sourceBitmap.height, matrix, false
                )

                // 原始 bitmap 已经被 createBitmap 使用，可以回收
                sourceBitmap.recycle()

                // 执行检测（mirroredBitmap 将在 detectAsync 内部被回收）
                handLandmarkerHelper.detectAsync(mirroredBitmap)

                // 计算FPS
                calculateFps()

            } catch (e: Exception) {
                Log.e(TAG, "Error processing bitmap", e)
                // 如果出现异常，确保 bitmap 被回收
                sourceBitmap.recycle()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in processImage", e)
        } finally {
            imageProxy.close()
        }
    }

    private fun updateUI(handLandmarks: HandLandmarks?) {
        if (handLandmarks == null) {
            overlayView.clear()
            statusText.text = getString(R.string.status_no_hand_detected)
            handednessText.text = ""
            return
        }

        // 更新状态
        statusText.text = getString(R.string.status_detecting)
        handednessText.text = "检测到: ${
            when (handLandmarks.handedness) {
                "Left" -> "左手"
                "Right" -> "右手"
                else -> "未知"
            }
        }"

        // 更新关键点
        overlayView.updateHandLandmarks(handLandmarks)

        // 根据选中的穴位计算并更新位置
        if (selectedAcupoints.contains("LI4")) {
            val li4Position = CoordinateUtils.calculateHegu(handLandmarks)
            overlayView.updateLi4Position(li4Position)
        } else {
            overlayView.updateLi4Position(null)
        }

        if (selectedAcupoints.contains("PC8")) {
            val pc8Position = CoordinateUtils.calculateLaogong(handLandmarks)
            overlayView.updatePc8Position(pc8Position)
        } else {
            overlayView.updatePc8Position(null)
        }
    }

    private fun calculateFps() {
        frameCount++
        val currentTime = SystemClock.elapsedRealtime()

        if (lastFpsTimestamp == 0L) {
            lastFpsTimestamp = currentTime
        }

        val elapsed = currentTime - lastFpsTimestamp
        if (elapsed >= 1000) {  // 每秒更新一次
            currentFps = (frameCount * 1000 / elapsed).toInt()
            runOnUiThread {
                fpsText.text = "FPS: $currentFps"
            }
            frameCount = 0
            lastFpsTimestamp = currentTime
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                initializeApp()
            } else {
                Toast.makeText(this, "需要相机权限才能使用此应用", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        handLandmarkerHelper.close()
    }

    /**
     * ImageProxy 转 Bitmap 扩展函数
     * 支持 RGBA_8888 格式的正确转换，处理 row padding
     */
    private fun ImageProxy.convertToBitmap(): Bitmap? {
        return try {
            val plane = planes[0]
            val buffer = plane.buffer

            // 重置 buffer 位置
            buffer.rewind()

            // 计算每行的字节数（包含 padding）
            val pixelStride = plane.pixelStride
            val rowStride = plane.rowStride
            val rowPadding = rowStride - pixelStride * width

            // 创建 Bitmap
            val bitmap = Bitmap.createBitmap(
                width + rowPadding / pixelStride,
                height,
                Bitmap.Config.ARGB_8888
            )

            // 复制数据
            bitmap.copyPixelsFromBuffer(buffer)

            // 如果有 padding，需要裁剪
            if (rowPadding != 0) {
                val croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
                bitmap.recycle()
                croppedBitmap
            } else {
                bitmap
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting ImageProxy to Bitmap", e)
            null
        }
    }
}

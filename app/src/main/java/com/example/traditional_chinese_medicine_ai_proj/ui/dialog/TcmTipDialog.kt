package com.example.traditional_chinese_medicine_ai_proj.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.utils.TcmTipHelper
import com.google.android.material.button.MaterialButton

/**
 * 中医提示语弹窗
 * 用于在用户完成任务时显示温暖的正向反馈
 *
 * 使用示例：
 * ```
 * TcmTipDialog.show(context, TcmTipHelper.Category.MASSAGE)
 * ```
 */
class TcmTipDialog(
    context: Context,
    private val category: String,
    private val points: Int = 0
) : Dialog(context, R.style.TcmTipDialogStyle) {

    private lateinit var tvTitle: TextView
    private lateinit var tvMessage: TextView
    private lateinit var tvPoints: TextView
    private lateinit var btnContinue: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_tcm_tip)

        // 设置弹窗宽度为屏幕宽度的85%
        window?.let {
            val params = it.attributes
            val displayMetrics = context.resources.displayMetrics
            params.width = (displayMetrics.widthPixels * 0.85).toInt()
            it.attributes = params
        }

        initViews()
        loadContent()
        setupAnimation()
        setupListeners()
    }

    private fun initViews() {
        tvTitle = findViewById(R.id.tvTitle)
        tvMessage = findViewById(R.id.tvMessage)
        tvPoints = findViewById(R.id.tvPoints)
        btnContinue = findViewById(R.id.btnContinue)
    }

    private fun loadContent() {
        // 加载标题和提示语
        val title = TcmTipHelper.getTitle(context, category)
        val tip = TcmTipHelper.getTip(context, category)

        tvTitle.text = title
        tvMessage.text = tip

        // 显示积分奖励（如果有）
        if (points > 0) {
            tvPoints.text = "+$points 积分"
            tvPoints.visibility = android.view.View.VISIBLE
        } else {
            tvPoints.visibility = android.view.View.GONE
        }
    }

    private fun setupAnimation() {
        // 添加淡入动画
        try {
            val fadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
            tvTitle.startAnimation(fadeIn)

            // 消息文本延迟一点显示
            tvMessage.postDelayed({
                tvMessage.startAnimation(fadeIn)
            }, 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupListeners() {
        btnContinue.setOnClickListener {
            dismiss()
        }

        // 点击外部关闭
        setCanceledOnTouchOutside(true)
    }

    companion object {
        /**
         * 显示提示弹窗（简化调用方式）
         * @param context Context
         * @param category 提示语分类，使用 TcmTipHelper.Category 常量
         * @param points 积分奖励，默认为0（不显示）
         * @param autoDismiss 是否自动消失（毫秒），默认不自动消失
         */
        @JvmStatic
        fun show(
            context: Context,
            category: String = TcmTipHelper.Category.DEFAULT,
            points: Int = 0,
            autoDismiss: Long? = null
        ) {
            try {
                val dialog = TcmTipDialog(context, category, points)
                dialog.show()

                // 如果设置了自动消失时间
                autoDismiss?.let { delay ->
                    dialog.window?.decorView?.postDelayed({
                        if (dialog.isShowing) {
                            dialog.dismiss()
                        }
                    }, delay)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * 显示提示弹窗并在指定时间后自动消失
         */
        @JvmStatic
        fun showAndDismiss(
            context: Context,
            category: String = TcmTipHelper.Category.DEFAULT,
            points: Int = 0,
            duration: Long = 2500L
        ) {
            show(context, category, points, duration)
        }
    }
}

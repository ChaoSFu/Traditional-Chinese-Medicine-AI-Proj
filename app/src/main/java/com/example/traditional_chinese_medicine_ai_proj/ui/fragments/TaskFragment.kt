package com.example.traditional_chinese_medicine_ai_proj.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.data.TodayTask
import com.example.traditional_chinese_medicine_ai_proj.adapter.TodayTaskAdapter
import com.example.traditional_chinese_medicine_ai_proj.utils.MockDataLoader
import com.example.traditional_chinese_medicine_ai_proj.utils.PointsManager
import com.example.traditional_chinese_medicine_ai_proj.utils.TaskNotificationManager
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

/**
 * 今日任务Fragment
 * 展示当天的健康任务清单
 */
class TaskFragment : Fragment() {

    private lateinit var tvDate: TextView
    private lateinit var tvTaskCount: TextView
    private lateinit var tvPoints: TextView
    private lateinit var recyclerTasks: RecyclerView

    private lateinit var taskAdapter: TodayTaskAdapter
    private val tasks = mutableListOf<TodayTask>()

    // 通知权限请求
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 权限已授予，设置任务提醒
            scheduleTaskNotifications()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 创建通知渠道
        TaskNotificationManager.createNotificationChannel(requireContext())

        initViews(view)
        loadData()
    }

    private fun initViews(view: View) {
        tvDate = view.findViewById(R.id.tvDate)
        tvTaskCount = view.findViewById(R.id.tvTaskCount)
        tvPoints = view.findViewById(R.id.tvPoints)
        recyclerTasks = view.findViewById(R.id.recyclerTasks)

        // 显示今日日期
        val dateFormat = SimpleDateFormat("yyyy年MM月dd日 E", Locale.CHINA)
        tvDate.text = dateFormat.format(Date())

        // 设置任务RecyclerView
        taskAdapter = TodayTaskAdapter(tasks) { task, position ->
            onTaskCompleted(task, position)
        }
        recyclerTasks.layoutManager = LinearLayoutManager(requireContext())
        recyclerTasks.adapter = taskAdapter
    }

    private fun loadData() {
        // 加载积分
        val points = PointsManager.getPoints(requireContext())
        tvPoints.text = "当前积分：$points"

        // 加载今日任务
        val taskList = MockDataLoader.loadTodayTasks(requireContext())
        taskList?.tasks?.let {
            tasks.clear()
            tasks.addAll(it)
            taskAdapter.notifyDataSetChanged()
            updateTaskCount()

            // 请求通知权限并设置任务提醒
            requestNotificationPermissionAndSchedule()
        }
    }

    /**
     * 请求通知权限并设置任务提醒
     */
    private fun requestNotificationPermissionAndSchedule() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 权限已授予
                    scheduleTaskNotifications()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // 显示权限说明后再请求
                    Toast.makeText(
                        requireContext(),
                        "需要通知权限以提醒您完成任务",
                        Toast.LENGTH_SHORT
                    ).show()
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // 直接请求权限
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Android 12 及以下不需要请求通知权限
            scheduleTaskNotifications()
        }
    }

    /**
     * 为所有未完成的任务设置提醒
     */
    private fun scheduleTaskNotifications() {
        TaskNotificationManager.scheduleAllTaskReminders(requireContext(), tasks)
    }

    private fun updateTaskCount() {
        val completedCount = tasks.count { it.completed }
        val totalCount = tasks.size
        tvTaskCount.text = "已完成 $completedCount/$totalCount"
    }

    private fun onTaskCompleted(task: TodayTask, position: Int) {
        if (!task.completed) {
            // 任务未完成 -> 标记为完成
            task.completed = true
            taskAdapter.notifyItemChanged(position)

            // 添加积分
            if (task.points > 0) {
                val newPoints = PointsManager.addPoints(requireContext(), task.points)
                tvPoints.text = "当前积分：$newPoints"
            }

            // 更新完成计数
            updateTaskCount()

            // 取消该任务的提醒通知
            TaskNotificationManager.cancelTaskReminder(requireContext(), task)

            // 根据任务类型选择提示分类
            val tipCategory = when (task.type) {
                "medicine" -> com.example.traditional_chinese_medicine_ai_proj.utils.TcmTipHelper.Category.HERB
                "massage" -> com.example.traditional_chinese_medicine_ai_proj.utils.TcmTipHelper.Category.MASSAGE
                "appointment" -> com.example.traditional_chinese_medicine_ai_proj.utils.TcmTipHelper.Category.APPOINTMENT
                "exercise" -> com.example.traditional_chinese_medicine_ai_proj.utils.TcmTipHelper.Category.EXERCISE
                "diet" -> com.example.traditional_chinese_medicine_ai_proj.utils.TcmTipHelper.Category.DIET
                "rest" -> com.example.traditional_chinese_medicine_ai_proj.utils.TcmTipHelper.Category.REST
                "record" -> com.example.traditional_chinese_medicine_ai_proj.utils.TcmTipHelper.Category.RECORD
                else -> com.example.traditional_chinese_medicine_ai_proj.utils.TcmTipHelper.Category.DEFAULT
            }

            // 显示温暖的提示弹窗（包含积分信息）
            com.example.traditional_chinese_medicine_ai_proj.ui.dialog.TcmTipDialog.show(
                requireContext(),
                tipCategory,
                task.points
            )
        } else {
            // 任务已完成 -> 取消完成
            task.completed = false
            taskAdapter.notifyItemChanged(position)

            // 回退积分
            if (task.points > 0) {
                val success = PointsManager.deductPoints(requireContext(), task.points)
                if (success) {
                    val newPoints = PointsManager.getPoints(requireContext())
                    tvPoints.text = "当前积分：$newPoints"
                    Toast.makeText(
                        requireContext(),
                        "已取消任务，积分-${task.points}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // 更新完成计数
            updateTaskCount()

            // 重新设置该任务的提醒
            TaskNotificationManager.scheduleTaskReminder(requireContext(), task)
        }
    }

    override fun onResume() {
        super.onResume()
        // 刷新积分显示
        val points = PointsManager.getPoints(requireContext())
        tvPoints.text = "当前积分：$points"
        // 刷新任务完成状态
        updateTaskCount()
    }
}

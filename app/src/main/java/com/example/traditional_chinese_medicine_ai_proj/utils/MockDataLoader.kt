package com.example.traditional_chinese_medicine_ai_proj.utils

import android.content.Context
import com.example.traditional_chinese_medicine_ai_proj.data.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Mock数据加载工具
 * 从assets/mock目录读取JSON文件
 */
object MockDataLoader {

    private val gson = Gson()

    /**
     * 加载医师列表
     */
    fun loadDoctors(context: Context): List<Doctor> {
        return try {
            val json = context.assets.open("mock/doctors.json")
                .bufferedReader()
                .use { it.readText() }
            val type = object : TypeToken<List<Doctor>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 加载预约排班表
     */
    fun loadAppointments(context: Context): Map<String, AppointmentSchedule> {
        return try {
            val json = context.assets.open("mock/appointments.json")
                .bufferedReader()
                .use { it.readText() }
            val type = object : TypeToken<Map<String, AppointmentSchedule>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    /**
     * 加载就诊记录
     */
    fun loadRecords(context: Context): List<MedicalRecord> {
        return try {
            val json = context.assets.open("mock/records.json")
                .bufferedReader()
                .use { it.readText() }
            val type = object : TypeToken<List<MedicalRecord>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 加载今日任务
     */
    fun loadTodayTasks(context: Context): TodayTaskList? {
        return try {
            val json = context.assets.open("mock/tasks_today.json")
                .bufferedReader()
                .use { it.readText() }
            gson.fromJson(json, TodayTaskList::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 加载用户信息
     */
    fun loadUser(context: Context): User? {
        return try {
            val json = context.assets.open("mock/user.json")
                .bufferedReader()
                .use { it.readText() }
            gson.fromJson(json, User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 根据ID获取医师信息
     */
    fun getDoctorById(context: Context, doctorId: Int): Doctor? {
        return loadDoctors(context).find { it.id == doctorId }
    }

    /**
     * 根据科室筛选医师
     */
    fun getDoctorsByDept(context: Context, dept: String): List<Doctor> {
        return loadDoctors(context).filter { it.dept == dept }
    }

    /**
     * 获取所有科室列表
     */
    fun getAllDepts(context: Context): List<String> {
        return loadDoctors(context).map { it.dept }.distinct()
    }

    /**
     * 加载讲座与活动列表
     */
    fun loadLectures(context: Context): List<Lecture> {
        return try {
            val json = context.assets.open("mock/lectures.json")
                .bufferedReader()
                .use { it.readText() }
            val type = object : TypeToken<List<Lecture>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 获取即将开始的讲座
     */
    fun getUpcomingLectures(context: Context, limit: Int = 3): List<Lecture> {
        return loadLectures(context)
            .filter { it.status == "upcoming" }
            .take(limit)
    }

    /**
     * 根据ID获取讲座信息
     */
    fun getLectureById(context: Context, lectureId: Int): Lecture? {
        return loadLectures(context).find { it.id == lectureId }
    }

    /**
     * 加载科室详细信息
     */
    fun loadDepartments(context: Context): List<Department> {
        return try {
            val json = context.assets.open("mock/departments.json")
                .bufferedReader()
                .use { it.readText() }
            val type = object : TypeToken<List<Department>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 根据科室名称获取科室详情
     */
    fun getDepartmentByName(context: Context, deptName: String): Department? {
        return loadDepartments(context).find { it.name == deptName }
    }

    /**
     * 根据ID获取科室详情
     */
    fun getDepartmentById(context: Context, deptId: Int): Department? {
        return loadDepartments(context).find { it.id == deptId }
    }

    /**
     * 加载积分商品列表
     */
    fun loadPointsProducts(context: Context): List<PointsProduct> {
        return try {
            val json = context.assets.open("mock/points_products.json")
                .bufferedReader()
                .use { it.readText() }
            val type = object : TypeToken<List<PointsProduct>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 根据类别获取积分商品
     */
    fun getProductsByCategory(context: Context, category: String): List<PointsProduct> {
        return loadPointsProducts(context).filter { it.category == category }
    }

    /**
     * 根据ID获取商品详情
     */
    fun getProductById(context: Context, productId: Int): PointsProduct? {
        return loadPointsProducts(context).find { it.id == productId }
    }
}

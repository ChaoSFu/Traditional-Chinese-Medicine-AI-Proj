package com.example.traditional_chinese_medicine_ai_proj

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.traditional_chinese_medicine_ai_proj.ui.fragments.HomeFragment
import com.example.traditional_chinese_medicine_ai_proj.ui.fragments.TaskFragment
import com.example.traditional_chinese_medicine_ai_proj.ui.fragments.DoctorFragment
import com.example.traditional_chinese_medicine_ai_proj.ui.fragments.StoreFragment
import com.example.traditional_chinese_medicine_ai_proj.ui.fragments.AiFragment
import com.example.traditional_chinese_medicine_ai_proj.ui.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * 中医就诊系统主界面
 * Main Activity with bottom navigation
 */
class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottom_navigation)

        // 设置底部导航监听
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_task -> {
                    loadFragment(TaskFragment())
                    true
                }
                R.id.nav_doctor -> {
                    loadFragment(DoctorFragment())
                    true
                }
                R.id.nav_store -> {
                    loadFragment(StoreFragment())
                    true
                }
                R.id.nav_ai -> {
                    loadFragment(AiFragment())
                    true
                }
                else -> false
            }
        }

        // 默认显示首页
        if (savedInstanceState == null) {
            bottomNavigation.selectedItemId = R.id.nav_home
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}

package com.example.traditional_chinese_medicine_ai_proj.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.adapter.ProductAdapter
import com.example.traditional_chinese_medicine_ai_proj.data.ProductCategory
import com.example.traditional_chinese_medicine_ai_proj.data.PointsProduct
import com.example.traditional_chinese_medicine_ai_proj.utils.MockDataLoader
import com.example.traditional_chinese_medicine_ai_proj.utils.PointsManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * 积分商城Fragment
 * 展示可兑换的中医礼品和课程
 */
class StoreFragment : Fragment() {

    private lateinit var tvPoints: TextView
    private lateinit var chipGroup: ChipGroup
    private lateinit var chipAll: Chip
    private lateinit var chipGift: Chip
    private lateinit var chipCourse: Chip
    private lateinit var recyclerProducts: RecyclerView

    private lateinit var productAdapter: ProductAdapter
    private val allProducts = mutableListOf<PointsProduct>()
    private val displayProducts = mutableListOf<PointsProduct>()

    private var currentCategory = "all"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_store, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        loadData()
        setupListeners()
    }

    private fun initViews(view: View) {
        tvPoints = view.findViewById(R.id.tvPoints)
        chipGroup = view.findViewById(R.id.chipGroup)
        chipAll = view.findViewById(R.id.chipAll)
        chipGift = view.findViewById(R.id.chipGift)
        chipCourse = view.findViewById(R.id.chipCourse)
        recyclerProducts = view.findViewById(R.id.recyclerProducts)

        // 设置RecyclerView网格布局（2列）
        productAdapter = ProductAdapter(displayProducts) { product ->
            onProductClick(product)
        }
        recyclerProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerProducts.adapter = productAdapter
    }

    private fun loadData() {
        // 加载用户积分
        val points = PointsManager.getPoints(requireContext())
        tvPoints.text = "我的积分：$points"

        // 加载商品数据
        val products = MockDataLoader.loadPointsProducts(requireContext())
        allProducts.clear()
        allProducts.addAll(products)

        filterProducts(currentCategory)
    }

    private fun setupListeners() {
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            currentCategory = when (checkedIds[0]) {
                R.id.chipGift -> ProductCategory.GIFT
                R.id.chipCourse -> ProductCategory.COURSE
                else -> "all"
            }
            filterProducts(currentCategory)
        }
    }

    private fun filterProducts(category: String) {
        displayProducts.clear()

        when (category) {
            "all" -> displayProducts.addAll(allProducts)
            else -> displayProducts.addAll(allProducts.filter { it.category == category })
        }

        productAdapter.notifyDataSetChanged()
    }

    private fun onProductClick(product: PointsProduct) {
        val currentPoints = PointsManager.getPoints(requireContext())

        // 检查积分是否足够
        if (currentPoints < product.points) {
            Toast.makeText(
                requireContext(),
                "积分不足！还需 ${product.points - currentPoints} 积分",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // 检查库存
        if (product.stock - product.redeemed <= 0) {
            Toast.makeText(requireContext(), "该商品已兑完", Toast.LENGTH_SHORT).show()
            return
        }

        // 显示兑换确认对话框
        AlertDialog.Builder(requireContext())
            .setTitle("确认兑换")
            .setMessage(
                "${product.name}\n\n" +
                "所需积分：${product.points}\n" +
                "剩余库存：${product.stock - product.redeemed}\n\n" +
                "确认兑换吗？"
            )
            .setPositiveButton("确认") { _, _ ->
                confirmRedeem(product)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun confirmRedeem(product: PointsProduct) {
        // 扣除积分
        val success = PointsManager.deductPoints(requireContext(), product.points)

        if (success) {
            // 更新积分显示
            val newPoints = PointsManager.getPoints(requireContext())
            tvPoints.text = "我的积分：$newPoints"

            // 显示温暖的提示弹窗
            com.example.traditional_chinese_medicine_ai_proj.ui.dialog.TcmTipDialog.show(
                requireContext(),
                com.example.traditional_chinese_medicine_ai_proj.utils.TcmTipHelper.Category.POINTS
            )
        } else {
            Toast.makeText(requireContext(), "积分不足，无法兑换", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // 刷新积分显示
        val points = PointsManager.getPoints(requireContext())
        tvPoints.text = "我的积分：$points"
    }
}

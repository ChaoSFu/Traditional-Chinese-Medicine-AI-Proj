package com.example.traditional_chinese_medicine_ai_proj.data

/**
 * 积分商品数据模型
 */
data class PointsProduct(
    val id: Int,
    val name: String,
    val category: String,          // 类别：gift(礼品), course(课程)
    val points: Int,                // 所需积分
    val description: String,
    val imageUrl: String = "",
    val stock: Int,                 // 库存数量
    val redeemed: Int = 0,          // 已兑换数量
    val tags: List<String> = emptyList()  // 标签
)

/**
 * 积分商品类别
 */
object ProductCategory {
    const val GIFT = "gift"
    const val COURSE = "course"
}

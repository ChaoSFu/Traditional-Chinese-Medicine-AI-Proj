package com.example.traditional_chinese_medicine_ai_proj.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.data.PointsProduct
import com.example.traditional_chinese_medicine_ai_proj.data.ProductCategory

/**
 * 积分商品Adapter
 */
class ProductAdapter(
    private val products: List<PointsProduct>,
    private val onProductClick: (PointsProduct) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProduct: ImageView = view.findViewById(R.id.ivProduct)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvPoints: TextView = view.findViewById(R.id.tvPoints)
        val tvStock: TextView = view.findViewById(R.id.tvStock)
        val tvTag: TextView = view.findViewById(R.id.tvTag)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        val context = holder.itemView.context

        holder.tvName.text = product.name
        holder.tvPoints.text = "${product.points} 积分"

        // 显示库存
        val remaining = product.stock - product.redeemed
        holder.tvStock.text = "库存：$remaining"

        // 根据库存设置颜色
        if (remaining <= 0) {
            holder.tvStock.setTextColor(context.getColor(android.R.color.holo_red_dark))
        } else if (remaining < 10) {
            holder.tvStock.setTextColor(context.getColor(android.R.color.holo_orange_dark))
        } else {
            holder.tvStock.setTextColor(context.getColor(android.R.color.darker_gray))
        }

        // 显示类别标签
        when (product.category) {
            ProductCategory.GIFT -> {
                holder.tvCategory.text = "礼品"
                holder.tvCategory.setBackgroundResource(R.drawable.bg_category_gift)
            }
            ProductCategory.COURSE -> {
                holder.tvCategory.text = "课程"
                holder.tvCategory.setBackgroundResource(R.drawable.bg_category_course)
            }
        }

        // 显示标签（如热销）
        if (product.tags.isNotEmpty()) {
            holder.tvTag.text = product.tags[0]
            holder.tvTag.visibility = View.VISIBLE

            // 根据标签设置样式
            when {
                product.tags.contains("热销") || product.tags.contains("热门课程") -> {
                    holder.tvTag.setBackgroundResource(R.drawable.bg_tag_hot)
                }
                product.tags.contains("精品") -> {
                    holder.tvTag.setBackgroundResource(R.drawable.bg_tag_premium)
                }
                else -> {
                    holder.tvTag.setBackgroundResource(R.drawable.bg_tag_default)
                }
            }
        } else {
            holder.tvTag.visibility = View.GONE
        }

        // 设置商品图标 - 根据商品名称选择合适的图标
        holder.ivProduct.setImageResource(getProductIcon(product))

        holder.itemView.setOnClickListener {
            onProductClick(product)
        }
    }

    override fun getItemCount() = products.size

    /**
     * 根据商品信息获取对应的图标资源
     */
    private fun getProductIcon(product: PointsProduct): Int {
        return when (product.id) {
            1 -> R.drawable.ic_product_moxibustion_patch  // 艾灸贴
            2 -> R.drawable.ic_product_herbal_tea         // 养生红枣枸杞茶
            3 -> R.drawable.ic_product_guasha             // 经络刮痧板
            4 -> R.drawable.ic_product_footbath           // 足浴养生药包
            5 -> R.drawable.ic_product_moxibustion        // 艾灸盒随身灸
            6 -> R.drawable.ic_product_tea                // 陈皮普洱茶饼
            7 -> R.drawable.ic_product_cupping            // 拔罐器
            8 -> R.drawable.ic_product_sachet             // 五行养生香囊
            9 -> R.drawable.ic_product_course_book        // 《黄帝内经》养生课程
            10 -> R.drawable.ic_product_tuina             // 小儿推拿入门课
            11 -> R.drawable.ic_product_acupoint          // 经络穴位认知课
            12 -> R.drawable.ic_product_tea               // 四季养生茶饮课
            13 -> R.drawable.ic_product_moxibustion       // 艾灸养生实操课
            14 -> R.drawable.ic_product_constitution      // 中医体质辨识课
            15 -> R.drawable.ic_product_tea               // 桑叶茶
            else -> {
                // 默认图标：根据类别
                when (product.category) {
                    ProductCategory.GIFT -> R.drawable.ic_gift
                    ProductCategory.COURSE -> R.drawable.ic_course
                    else -> R.drawable.ic_gift
                }
            }
        }
    }
}

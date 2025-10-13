package com.example.traditional_chinese_medicine_ai_proj

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

/**
 * 穴位列表适配器
 */
class AcupointAdapter(
    private val acupoints: List<AcupointSelectionActivity.AcupointInfo>,
    private val selectedAcupoints: MutableSet<String>,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<AcupointAdapter.AcupointViewHolder>() {

    class AcupointViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: MaterialCardView = view.findViewById(R.id.cardView)
        val checkbox: CheckBox = view.findViewById(R.id.checkboxAcupoint)
        val nameCn: TextView = view.findViewById(R.id.textAcupointNameCn)
        val nameEn: TextView = view.findViewById(R.id.textAcupointNameEn)
        val meridian: TextView = view.findViewById(R.id.textMeridian)
        val location: TextView = view.findViewById(R.id.textLocation)
        val functions: TextView = view.findViewById(R.id.textFunctions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcupointViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_acupoint, parent, false)
        return AcupointViewHolder(view)
    }

    override fun onBindViewHolder(holder: AcupointViewHolder, position: Int) {
        val acupoint = acupoints[position]

        holder.nameCn.text = acupoint.nameCn
        holder.nameEn.text = "${acupoint.id} - ${acupoint.nameEn}"
        holder.meridian.text = "经络：${acupoint.meridian}"
        holder.location.text = "定位：${acupoint.location}"
        holder.functions.text = "主治：${acupoint.functions.joinToString("、")}"

        // 设置复选框状态
        holder.checkbox.isChecked = selectedAcupoints.contains(acupoint.id)

        // 复选框点击事件
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedAcupoints.add(acupoint.id)
            } else {
                selectedAcupoints.remove(acupoint.id)
            }
            onSelectionChanged()
        }

        // 卡片点击切换选中状态
        holder.cardView.setOnClickListener {
            holder.checkbox.isChecked = !holder.checkbox.isChecked
        }
    }

    override fun getItemCount() = acupoints.size
}

package com.example.traditional_chinese_medicine_ai_proj

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * 身体部位列表适配器
 */
class BodyPartAdapter(
    private val bodyParts: List<BodyPartSelectionActivity.BodyPart>,
    private val onItemClick: (BodyPartSelectionActivity.BodyPart) -> Unit
) : RecyclerView.Adapter<BodyPartAdapter.BodyPartViewHolder>() {

    class BodyPartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardView)
        val icon: ImageView = view.findViewById(R.id.iconBodyPart)
        val name: TextView = view.findViewById(R.id.textBodyPartName)
        val status: TextView = view.findViewById(R.id.textBodyPartStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BodyPartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_body_part, parent, false)
        return BodyPartViewHolder(view)
    }

    override fun onBindViewHolder(holder: BodyPartViewHolder, position: Int) {
        val bodyPart = bodyParts[position]

        holder.icon.setImageResource(bodyPart.iconRes)
        holder.name.text = bodyPart.name
        holder.status.text = bodyPart.status

        // 设置可用/不可用的视觉效果
        if (bodyPart.isAvailable) {
            holder.cardView.alpha = 1.0f
            holder.status.setTextColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark)
            )
        } else {
            holder.cardView.alpha = 0.6f
            holder.status.setTextColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray)
            )
        }

        holder.cardView.setOnClickListener {
            onItemClick(bodyPart)
        }
    }

    override fun getItemCount() = bodyParts.size
}

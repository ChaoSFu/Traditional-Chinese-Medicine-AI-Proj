package com.example.traditional_chinese_medicine_ai_proj.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.data.Comment

class CommentAdapter(
    private val comments: List<Comment>,
    private val onCommentLike: (Comment) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivUserAvatar: ImageView = view.findViewById(R.id.ivUserAvatar)
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val tvUserRole: TextView = view.findViewById(R.id.tvUserRole)
        val tvCommentTime: TextView = view.findViewById(R.id.tvCommentTime)
        val tvCommentText: TextView = view.findViewById(R.id.tvCommentText)
        val ivCommentLike: ImageView = view.findViewById(R.id.ivCommentLike)
        val tvCommentLikes: TextView = view.findViewById(R.id.tvCommentLikes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        holder.tvUserName.text = comment.user
        holder.tvCommentTime.text = comment.time
        holder.tvCommentText.text = comment.text
        holder.tvCommentLikes.text = comment.likes.toString()

        // 根据角色显示标签
        if (comment.role == "医生") {
            holder.tvUserRole.visibility = View.VISIBLE
            holder.tvUserRole.text = comment.role
            holder.tvUserRole.setBackgroundResource(R.drawable.bg_tag_premium)
        } else {
            holder.tvUserRole.visibility = View.GONE
        }

        // 设置点赞状态
        if (comment.isLiked) {
            holder.ivCommentLike.setColorFilter(holder.itemView.context.getColor(R.color.tcm_primary))
            holder.tvCommentLikes.setTextColor(holder.itemView.context.getColor(R.color.tcm_primary))
        } else {
            holder.ivCommentLike.setColorFilter(holder.itemView.context.getColor(android.R.color.darker_gray))
            holder.tvCommentLikes.setTextColor(holder.itemView.context.getColor(android.R.color.darker_gray))
        }

        // 点赞点击事件
        val likeClickListener = View.OnClickListener {
            onCommentLike(comment)
        }
        holder.ivCommentLike.setOnClickListener(likeClickListener)
        holder.tvCommentLikes.setOnClickListener(likeClickListener)
    }

    override fun getItemCount() = comments.size
}

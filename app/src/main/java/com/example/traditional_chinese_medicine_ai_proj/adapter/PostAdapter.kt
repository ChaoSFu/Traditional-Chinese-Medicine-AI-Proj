package com.example.traditional_chinese_medicine_ai_proj.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.data.Post

class PostAdapter(
    private val posts: List<Post>,
    private val onPostClick: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAuthorAvatar: ImageView = view.findViewById(R.id.ivAuthorAvatar)
        val tvAuthorName: TextView = view.findViewById(R.id.tvAuthorName)
        val tvAuthorRole: TextView = view.findViewById(R.id.tvAuthorRole)
        val tvPostTime: TextView = view.findViewById(R.id.tvPostTime)
        val tvPostTitle: TextView = view.findViewById(R.id.tvPostTitle)
        val tvPostContent: TextView = view.findViewById(R.id.tvPostContent)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvComments: TextView = view.findViewById(R.id.tvComments)
        val tvLikes: TextView = view.findViewById(R.id.tvLikes)
        val tvViews: TextView = view.findViewById(R.id.tvViews)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.tvAuthorName.text = post.author
        holder.tvAuthorRole.text = post.role
        holder.tvPostTime.text = post.time
        holder.tvPostTitle.text = post.title
        holder.tvPostContent.text = post.content
        holder.tvCategory.text = "# ${post.category}"
        holder.tvComments.text = post.comments.toString()
        holder.tvLikes.text = post.likes.toString()
        holder.tvViews.text = post.views.toString()

        // 根据角色设置不同的标签样式
        if (post.role == "医生") {
            holder.tvAuthorRole.setBackgroundResource(R.drawable.bg_tag_premium)
        } else {
            holder.tvAuthorRole.setBackgroundResource(R.drawable.bg_tag_default)
        }

        // 点击事件
        holder.itemView.setOnClickListener {
            onPostClick(post)
        }
    }

    override fun getItemCount() = posts.size
}

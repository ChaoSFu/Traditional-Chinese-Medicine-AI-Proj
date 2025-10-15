package com.example.traditional_chinese_medicine_ai_proj.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.api.MessageUI
import java.text.SimpleDateFormat
import java.util.*

/**
 * 聊天消息适配器
 */
class ChatAdapter : ListAdapter<MessageUI, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_ASSISTANT = 2
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        private fun formatTime(timestamp: Long): String {
            return timeFormat.format(Date(timestamp))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).role) {
            "user" -> VIEW_TYPE_USER
            else -> VIEW_TYPE_ASSISTANT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_user, parent, false)
                UserMessageViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_assistant, parent, false)
                AssistantMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is AssistantMessageViewHolder -> holder.bind(message)
        }
    }

    /**
     * 用户消息 ViewHolder
     */
    class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)

        fun bind(message: MessageUI) {
            messageText.text = message.content
            timeText.text = formatTime(message.timestamp)
        }
    }

    /**
     * AI 助手消息 ViewHolder
     */
    class AssistantMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)

        fun bind(message: MessageUI) {
            messageText.text = message.content
            timeText.text = formatTime(message.timestamp)
        }
    }
}

/**
 * DiffUtil 回调
 */
class MessageDiffCallback : DiffUtil.ItemCallback<MessageUI>() {
    override fun areItemsTheSame(oldItem: MessageUI, newItem: MessageUI): Boolean {
        return oldItem.timestamp == newItem.timestamp
    }

    override fun areContentsTheSame(oldItem: MessageUI, newItem: MessageUI): Boolean {
        return oldItem == newItem
    }
}

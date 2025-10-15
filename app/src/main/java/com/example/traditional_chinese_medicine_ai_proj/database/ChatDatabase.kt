package com.example.traditional_chinese_medicine_ai_proj.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * 聊天数据库
 */
@Database(
    entities = [ChatMessage::class],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getDatabase(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "chat_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

# 问答记录本地保存功能

## 功能概述

使用 Room 数据库实现聊天记录的本地持久化存储，用户关闭应用后再次打开，历史对话记录会自动加载。

## 技术架构

### 数据库层次结构

```
ChatDatabase (Room Database)
    ↓
ChatDao (Data Access Object)
    ↓
ChatRepository (数据仓库)
    ↓
ChatViewModel (状态管理)
    ↓
ChatActivity (UI 界面)
```

## 核心组件

### 1. ChatMessage (实体类)

数据库表结构：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| role | String | 角色："user" 或 "assistant" |
| content | String | 消息内容 |
| timestamp | Long | 时间戳（毫秒） |

```kotlin
@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
```

### 2. ChatDao (数据访问对象)

提供数据库操作方法：

```kotlin
interface ChatDao {
    @Insert
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    suspend fun getAllMessages(): List<ChatMessage>

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()

    // ... 更多方法
}
```

### 3. ChatDatabase (数据库类)

```kotlin
@Database(entities = [ChatMessage::class], version = 1)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        private var INSTANCE: ChatDatabase? = null

        fun getDatabase(context: Context): ChatDatabase {
            // 单例模式
        }
    }
}
```

### 4. ChatRepository (数据仓库)

封装数据访问逻辑，负责 MessageUI 和 ChatMessage 之间的转换：

```kotlin
class ChatRepository(private val chatDao: ChatDao) {

    // 保存消息
    suspend fun saveMessage(messageUI: MessageUI) {
        val chatMessage = ChatMessage(
            role = messageUI.role,
            content = messageUI.content,
            timestamp = messageUI.timestamp
        )
        chatDao.insertMessage(chatMessage)
    }

    // 加载消息
    suspend fun getAllMessages(): List<MessageUI> {
        return chatDao.getAllMessages().map { ... }
    }

    // 清空消息
    suspend fun clearAllMessages() {
        chatDao.deleteAllMessages()
    }
}
```

### 5. ChatViewModel (状态管理)

从 `ViewModel` 改为 `AndroidViewModel`，支持获取 Application Context：

```kotlin
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository

    init {
        val chatDao = ChatDatabase.getDatabase(application).chatDao()
        repository = ChatRepository(chatDao)

        // 自动加载历史消息
        loadHistoryMessages()
    }

    // 发送消息时保存到数据库
    fun askQuestion(question: String, context: Context) {
        val userMessage = MessageUI("user", question)

        // 保存到数据库
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveMessage(userMessage)
        }

        // ... API 调用 ...
    }

    // 清空对话时删除数据库记录
    fun clearConversation() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllMessages()
        }
    }
}
```

## 数据流程

### 1. 应用启动流程

```
App 启动
  ↓
ChatActivity 创建 ChatViewModel
  ↓
ChatViewModel.init() 执行
  ↓
loadHistoryMessages() 从数据库加载
  ↓
Repository.getAllMessages()
  ↓
ChatDao.getAllMessages()
  ↓
查询数据库表 chat_messages
  ↓
转换 ChatMessage → MessageUI
  ↓
更新 LiveData<List<MessageUI>>
  ↓
ChatActivity 观察到数据变化
  ↓
ChatAdapter 显示历史消息
```

### 2. 发送消息流程

```
用户输入问题 → 点击发送
  ↓
ChatViewModel.askQuestion()
  ↓
创建 MessageUI("user", question)
  ↓
【并行操作1】UI 更新
_messages.value += userMessage
  ↓
【并行操作2】保存到数据库
repository.saveMessage(userMessage)
  ↓
ChatDao.insertMessage()
  ↓
插入到 chat_messages 表
  ↓
调用 ChatGPT API
  ↓
收到 AI 回复
  ↓
创建 MessageUI("assistant", answer)
  ↓
【并行操作1】UI 更新
_messages.value += assistantMessage
  ↓
【并行操作2】保存到数据库
repository.saveMessage(assistantMessage)
```

### 3. 清空对话流程

```
用户点击"清空对话"
  ↓
ChatViewModel.clearConversation()
  ↓
【操作1】清空内存数据
_messages.value = emptyList()
conversationHistory.clear()
  ↓
【操作2】清空数据库
repository.clearAllMessages()
  ↓
ChatDao.deleteAllMessages()
  ↓
DELETE FROM chat_messages
```

## 依赖配置

### gradle/libs.versions.toml

```toml
[versions]
room = "2.6.1"

[libraries]
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
```

### app/build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")  // 必须添加
}

dependencies {
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)  // 使用 kapt 处理注解
}
```

## 数据持久化特性

### 1. 自动加载

- 应用启动时自动从数据库加载历史记录
- 无需用户手动操作
- 即使应用被完全关闭，数据也不会丢失

### 2. 实时保存

- 每条消息发送后立即保存到数据库
- 包括用户消息和 AI 回复
- 使用协程异步保存，不阻塞 UI

### 3. 完整删除

- "清空对话"会同时清除内存和数据库
- 确保数据一致性
- 删除操作不可恢复

### 4. 线程安全

- 所有数据库操作都在 IO 线程执行
- 使用 `Dispatchers.IO` 避免阻塞主线程
- LiveData 自动在主线程更新 UI

## 文件清单

### 新增文件

1. `database/ChatMessage.kt` - 数据库实体类
2. `database/ChatDao.kt` - 数据访问接口
3. `database/ChatDatabase.kt` - Room 数据库
4. `repository/ChatRepository.kt` - 数据仓库
5. `CHAT_HISTORY_FEATURE.md` - 本文档

### 修改文件

1. `viewmodel/ChatViewModel.kt` - 集成数据库功能
   - 改为 AndroidViewModel
   - 添加 Repository
   - 添加加载历史消息
   - 发送消息时保存
   - 清空对话时删除

2. `app/build.gradle.kts` - 添加 Room 依赖和 kapt 插件

3. `gradle/libs.versions.toml` - 添加 Room 版本管理

## 使用示例

### 场景 1: 用户第一次使用

```
打开 App → 聊天界面为空 → 发送问题 → 收到回复
关闭 App → 重新打开 → 历史对话自动显示 ✅
```

### 场景 2: 多次对话

```
打开 App → 看到历史记录（10 条消息）
继续提问 → 新消息追加到列表
关闭 App → 重新打开 → 所有 12 条消息都在 ✅
```

### 场景 3: 清空对话

```
打开 App → 看到历史记录
点击"清空对话" → 确认
界面清空 → 关闭 App → 重新打开
聊天界面为空 ✅ （数据库已删除）
```

## 性能优化

1. **协程异步操作**：所有数据库操作使用 `suspend` 函数
2. **批量插入支持**：提供 `insertMessages()` 方法
3. **索引优化**：按 timestamp 排序，可添加索引
4. **懒加载支持**：提供 `getRecentMessages(limit)` 限制查询条数
5. **Flow 支持**：提供 `getAllMessagesFlow()` 实时监听数据变化

## 未来扩展

### 可能的增强功能

1. **对话会话管理**：
   - 支持多个对话会话
   - 每个会话独立保存
   - 可切换不同会话

2. **消息搜索**：
   - 全文搜索历史消息
   - 按关键词过滤

3. **导出功能**：
   - 导出对话为 TXT/PDF
   - 分享对话记录

4. **云端同步**：
   - 结合 Firebase/自建服务器
   - 多设备同步对话

5. **数据统计**：
   - 统计提问次数
   - 统计 Token 使用量
   - 统计费用

## 注意事项

1. **数据隐私**：所有对话记录仅保存在本地，不会上传到服务器
2. **存储空间**：长期使用可能占用较多空间，建议定期清理
3. **版本升级**：使用 `.fallbackToDestructiveMigration()` 简化升级，但会清空数据
4. **API Key 安全**：API Key 仍保存在 SharedPreferences，与对话记录分开

---

**版本**: v1.0
**完成时间**: 2025-01
**技术栈**: Room 2.6.1, Kotlin Coroutines, LiveData, AndroidViewModel

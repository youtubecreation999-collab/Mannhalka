package com.example.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FeelingPostDao {
    @Query("SELECT * FROM feeling_posts ORDER BY timestamp DESC")
    fun getAllFeelingPosts(): Flow<List<FeelingPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeelingPost(post: FeelingPost)

    @Query("DELETE FROM feeling_posts WHERE id = :id")
    suspend fun deleteFeelingPost(id: Long)

    @Query("DELETE FROM feeling_posts")
    suspend fun deleteAllFeelingPosts()
}

@Dao
interface ChatRoomDao {
    @Query("SELECT * FROM chat_rooms ORDER BY lastMessageTimestamp DESC")
    fun getAllChatRooms(): Flow<List<ChatRoom>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatRoom(room: ChatRoom)

    @Query("UPDATE chat_rooms SET lastMessage = :message, lastMessageTimestamp = :timestamp WHERE chatId = :chatId")
    suspend fun updateLastMessage(chatId: String, message: String, timestamp: Long)

    @Query("SELECT * FROM chat_rooms WHERE chatId = :chatId LIMIT 1")
    suspend fun getChatRoomById(chatId: String): ChatRoom?

    @Query("DELETE FROM chat_rooms")
    suspend fun deleteAllChatRooms()
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE expiresAt IS NOT NULL AND expiresAt < :currentTime")
    suspend fun deleteExpiredMessages(currentTime: Long)

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()
}

@Dao
interface AppSettingDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: AppSetting)
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Query("DELETE FROM contacts")
    suspend fun deleteAllContacts()
}

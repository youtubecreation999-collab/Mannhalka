package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feeling_posts")
data class FeelingPost(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val emotionTag: String,
    val timestamp: Long = System.currentTimeMillis(),
    val authorName: String,
    val authorAvatarColor: Int
)

@Entity(tableName = "chat_rooms")
data class ChatRoom(
    @PrimaryKey val chatId: String,
    val participantName: String,
    val participantAvatarColor: Int,
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: String,
    val senderName: String,
    val encryptedContent: String,
    val iv: String,
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null
)

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val mobileNumber: String,
    val name: String,
    val avatarColor: Int
)

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

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val userId: String = "current_user",
    val score: Long = 0,
    val league: String = "BRONZE",
    val rewardAudioCallMinutes: Int = 0
)

@Entity(tableName = "reward_history")
data class RewardHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String = "Redeemed 5-minute call"
)

@Entity(tableName = "privacy_logs")
data class PrivacyLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String
)

@Entity(tableName = "security_logs")
data class SecurityLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String
)

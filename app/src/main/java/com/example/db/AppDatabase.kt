package com.example.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FeelingPost::class, ChatRoom::class, ChatMessage::class, AppSetting::class, ContactEntity::class, UserStats::class, RewardHistory::class, PrivacyLog::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feelingPostDao(): FeelingPostDao
    abstract fun chatRoomDao(): ChatRoomDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun appSettingDao(): AppSettingDao
    abstract fun contactDao(): ContactDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun rewardHistoryDao(): RewardHistoryDao
    abstract fun privacyLogDao(): PrivacyLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mannhalka_database"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

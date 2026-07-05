package com.example.db

import com.example.security.CryptoHelper
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val feelingPostDao: FeelingPostDao,
    private val chatRoomDao: ChatRoomDao,
    private val chatMessageDao: ChatMessageDao,
    private val appSettingDao: AppSettingDao,
    private val contactDao: ContactDao
) {
    // Feel Posts
    val allFeelingPosts: Flow<List<FeelingPost>> = feelingPostDao.getAllFeelingPosts()

    suspend fun insertFeelingPost(post: FeelingPost) {
        feelingPostDao.insertFeelingPost(post)
    }

    suspend fun deleteFeelingPost(id: Long) {
        feelingPostDao.deleteFeelingPost(id)
    }

    // Chat Rooms
    val allChatRooms: Flow<List<ChatRoom>> = chatRoomDao.getAllChatRooms()

    suspend fun createChatRoom(room: ChatRoom) {
        chatRoomDao.insertChatRoom(room)
    }

    suspend fun getChatRoomById(chatId: String): ChatRoom? {
        return chatRoomDao.getChatRoomById(chatId)
    }

    // Chat Messages
    fun getMessagesForChat(chatId: String): Flow<List<ChatMessage>> {
        return chatMessageDao.getMessagesForChat(chatId)
    }

    suspend fun sendSecureMessage(chatId: String, senderName: String, cleartext: String) {
        // Encrypt the cleartext message with the unique chatId as seed
        val (encryptedText, iv) = CryptoHelper.encrypt(cleartext, chatId)
        
        val message = ChatMessage(
            chatId = chatId,
            senderName = senderName,
            encryptedContent = encryptedText,
            iv = iv,
            timestamp = System.currentTimeMillis()
        )
        
        chatMessageDao.insertMessage(message)
        
        // Update the chat room with a secure snippet
        chatRoomDao.updateLastMessage(chatId, cleartext, message.timestamp)
    }

    suspend fun deleteAllMessages() {
        chatMessageDao.deleteAllMessages()
    }

    // App Settings
    suspend fun getSetting(key: String): String? {
        return appSettingDao.getSetting(key)
    }

    suspend fun saveSetting(key: String, value: String) {
        appSettingDao.insertSetting(AppSetting(key, value))
    }

    // Contacts
    val allContacts = contactDao.getAllContacts()

    suspend fun insertContact(contact: ContactEntity) {
        contactDao.insertContact(contact)
    }
}

package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.db.*
import com.example.security.CryptoHelper
import com.example.security.GeminiModerator
import com.example.security.IntegrityGuard
import com.example.security.MockEncryptionEngine
import com.example.security.NotificationHelper
import com.example.BuildConfig
import com.example.db.ContactEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.UUID
import kotlin.random.Random

sealed interface Screen {
    object Auth : Screen
    object PasscodeSetup : Screen
    object ProfileSetup : Screen
    object Feed : Screen
    object Share : Screen
    object ChatList : Screen
    data class ChatRoomScreen(val chatId: String) : Screen
    object Settings : Screen
    object Dashboard : Screen
    object Leaderboard : Screen
    object Profile : Screen
    object MobileSettings : Screen
}

sealed interface ModerationState {
    object Idle : ModerationState
    object Analyzing : ModerationState
    data class Approved(val message: String) : ModerationState
    data class Blocked(val reason: String) : ModerationState
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = AppRepository(
        database.feelingPostDao(),
        database.chatRoomDao(),
        database.chatMessageDao(),
        database.appSettingDao(),
        database.contactDao(),
        database.userStatsDao()
    )

    // Navigation and Authentication state
    val currentScreen = MutableStateFlow<Screen>(Screen.Auth)
    val isAuthenticated = MutableStateFlow(false)
    val isPasscodeSetup = MutableStateFlow(false)
    val passcodeText = MutableStateFlow("")
    val passcodeSetupText = MutableStateFlow("")
    val passcodeSetupConfirmText = MutableStateFlow("")
    val passcodeError = MutableStateFlow<String?>(null)

    // Integrity Guard live diagnostics states
    val isRooted = MutableStateFlow(false)
    val isDebuggerConnected = MutableStateFlow(false)
    val isEmulatorRunning = MutableStateFlow(false)
    val isBiometricSupported = MutableStateFlow(false)
    val defenseShieldEnabled = MutableStateFlow(true)
    val selfHealedFlag = MutableStateFlow(false)
    val lastCrashReason = MutableStateFlow("")

    // Anonymous User profile
    val userPseudonym = MutableStateFlow("")
    val userAvatarColor = MutableStateFlow(0xFF00796B.toInt()) // Default Teal
    val userAvatarIndex = MutableStateFlow(0)
    val user2FaSecret = MutableStateFlow<String?>(null)
    val isBiometricEnabled = MutableStateFlow(false)

    // Leaderboard
    val userStats = repository.userStats.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStats())

    fun updateScore(points: Int) {
        viewModelScope.launch {
            val currentStats = userStats.value ?: UserStats()
            val newScore = currentStats.score + points.toLong()
            val leagues = listOf("BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "BOSS", "KING", "EMPEROR")
            
            // Simplified progression: every 100 points moves a league
            val newLeagueIndex = ((newScore / 100).toInt()).coerceIn(0, leagues.size - 1)
            val newLeague = leagues[newLeagueIndex]
            
            var rewardMinutes = currentStats.rewardAudioCallMinutes
            // Top 3 reward logic simulation (based on score threshold)
            if (newLeagueIndex > 0 && currentStats.league != newLeague) {
                rewardMinutes += 5
            }
            
            repository.saveUserStats(currentStats.copy(score = newScore, league = newLeague, rewardAudioCallMinutes = rewardMinutes))
        }
    }

    fun findNewFriend(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val prompt = "Find a friend for me to talk to based on my interests."
            val apiKey = BuildConfig.GEMINI_API_KEY
            val request = GenerateContentRequest(
                contents = listOf(Content(
                    parts = listOf(Part(text = prompt))
                ))
            )
            try {
                val response = RetrofitClient.service.generateContent(apiKey, request)
                val friend = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No friend found"
                NotificationHelper.showNotification(getApplication(), "Chat Match Found!", "Gemini found a new friend for you: $friend")
                onResult(friend)
            } catch (e: Exception) {
                onResult("Error: ${e.message}")
            }
        }
    }

    // Emotional Feed Filter tag
    val selectedTagFilter = MutableStateFlow<String?>(null)
    private val allFeelingPosts = repository.allFeelingPosts

    val filteredFeelingPosts = combine(allFeelingPosts, selectedTagFilter) { posts, tag ->
        if (tag == null) posts else posts.filter { it.emotionTag == tag }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Direct Secure Chats
    val allChatRooms = repository.allChatRooms
    val activeChatId = MutableStateFlow<String?>(null)
    
    val activeChatMessages = activeChatId.flatMapLatest { id ->
        if (id != null) repository.getMessagesForChat(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeChatRoom = activeChatId.flatMapLatest { id ->
        flow {
            if (id != null) {
                emit(repository.getChatRoomById(id))
            } else {
                emit(null)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Sharing and AI Content Moderation state
    val postText = MutableStateFlow("")
    val postSelectedTag = MutableStateFlow("Venting")
    val moderationStatus = MutableStateFlow<ModerationState>(ModerationState.Idle)
    val chatModerationStatus = MutableStateFlow<ModerationState>(ModerationState.Idle)

    // Encryption View Overlays Map (messageId -> showEncryptedState)
    val expandedMessageEncryptionState = MutableStateFlow<Map<Long, Boolean>>(emptyMap())
    
    // Call state
    val isCalling = MutableStateFlow(false)
    val timeRemaining = MutableStateFlow(120L) // 2 minutes in seconds
    private var callJob: kotlinx.coroutines.Job? = null

    fun startCall() {
        if (isCalling.value) return
        isCalling.value = true
        timeRemaining.value = 120L
        callJob = viewModelScope.launch {
            while (timeRemaining.value > 0) {
                kotlinx.coroutines.delay(1000)
                timeRemaining.value -= 1
            }
            isCalling.value = false
        }
    }
    
    // Auto-lock mechanism
    val isLocked = MutableStateFlow(false)
    val isAuthenticating = MutableStateFlow(false)
    val lastInteractionTime = MutableStateFlow(System.currentTimeMillis())
    private val timeoutDuration = 300000L // 5 minutes

    // Privacy Mode
    val isPrivacyMode = MutableStateFlow(false)
    fun setPrivacyMode(enabled: Boolean) {
        isPrivacyMode.value = enabled
    }

    val icebreakers = listOf(
        "Hi there! Looking forward to chatting.",
        "What's on your mind today?",
        "Hello! How are you doing?",
        "Hope you're having a great day!"
    )

    // E2EE State
    val userPublicKey = MutableStateFlow(MockEncryptionEngine.publicKey)
    val peerPublicKey = MutableStateFlow<String?>(null)
    val sharedSecret = MutableStateFlow<String?>(null)
    
    // Mobile Number System
    val userMobileNumber = MutableStateFlow<String?>(null)
    val contacts = repository.allContacts

    // Download link state
    val downloadLink = MutableStateFlow<String?>(null)

    // TTL Setting
    val messageTtl = MutableStateFlow<Long?>(null) // null means no self-destruct

    // Dynamic Theme Setting
    val selectedThemeId = MutableStateFlow("sunset_coral")

    init {
        viewModelScope.launch {
            // Load mobile number
            userMobileNumber.value = repository.getSetting("USER_MOBILE_NUMBER")
            
            // Live-check active security parameters
            isRooted.value = IntegrityGuard.isDeviceRooted()
            isDebuggerConnected.value = IntegrityGuard.isDebuggerAttached()
            isEmulatorRunning.value = IntegrityGuard.isEmulator()
            
            // Inactivity monitor
            launch {
                while (true) {
                    if (isAuthenticated.value && !isLocked.value) {
                        if (System.currentTimeMillis() - lastInteractionTime.value > timeoutDuration) {
                            lockApp()
                        }
                    }
                    kotlinx.coroutines.delay(1000)
                }
            }
            
            // Query biometric availability
            com.example.security.BiometricBridge.canAuthenticate(application, object : com.example.security.BiometricBridge.BiometricPromise {
                override fun resolve(value: Any?) {
                    isBiometricSupported.value = value == true
                }
                override fun reject(code: String, message: String) {
                    isBiometricSupported.value = false
                }
            })
            
            // Query self-healing state
            val prefs = application.getSharedPreferences("mannhalka_security_enclave", Context.MODE_PRIVATE)
            if (prefs.getBoolean("self_healed_flag", false)) {
                selfHealedFlag.value = true
                lastCrashReason.value = prefs.getString("last_error_message", "Unexpected hardware failure") ?: ""
            }

            loadUserProfile()
            checkPasscodeSetup()
            prepopulateDemoDataIfNeeded()
            
            // Load TTL setting
            val ttlStr = repository.getSetting("message_ttl")
            messageTtl.value = ttlStr?.toLongOrNull()

            // Load theme setting
            val themeStr = repository.getSetting("selected_theme_id")
            selectedThemeId.value = if (themeStr.isNullOrEmpty()) "sunset_coral" else themeStr
            
            // Clean up expired messages on startup
            repository.cleanupExpiredMessages()
        }
    }

    fun setMessageTtl(ttlMillis: Long?) {
        viewModelScope.launch {
            messageTtl.value = ttlMillis
            repository.saveSetting("message_ttl", ttlMillis?.toString() ?: "")
        }
    }

    fun setSelectedThemeId(themeId: String) {
        viewModelScope.launch {
            selectedThemeId.value = themeId
            repository.saveSetting("selected_theme_id", themeId)
        }
    }

    private suspend fun checkPasscodeSetup() {
        val pin = repository.getSetting("user_pin")
        if (pin.isNullOrEmpty()) {
            isPasscodeSetup.value = false
            currentScreen.value = Screen.ProfileSetup
        } else {
            isPasscodeSetup.value = true
            currentScreen.value = Screen.Auth
        }
    }

    private suspend fun getOrCreateCryptoSalt(): String {
        var salt = repository.getSetting("crypto_salt")
        if (salt.isNullOrEmpty()) {
            salt = UUID.randomUUID().toString()
            repository.saveSetting("crypto_salt", salt)
        }
        return salt
    }

    fun dismissSelfHealNotice() {
        viewModelScope.launch {
            selfHealedFlag.value = false
            val prefs = getApplication<Application>().getSharedPreferences("mannhalka_security_enclave", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("self_healed_flag", false).apply()
        }
    }

    fun wipeEnclaveAndWipeData() {
        viewModelScope.launch {
            repository.saveSetting("user_pin", "")
            repository.saveSetting("profile_pseudonym", "")
            repository.saveSetting("profile_color", "")
            repository.saveSetting("profile_avatar_index", "0")
            repository.saveSetting("profile_2fa_secret", "")
            repository.saveSetting("profile_biometric_enabled", "false")
            isPasscodeSetup.value = false
            isAuthenticated.value = false
            currentScreen.value = Screen.ProfileSetup
        }
    }

    private suspend fun loadUserProfile() {
        var name = repository.getSetting("profile_pseudonym")
        var colorStr = repository.getSetting("profile_color")
        val avatarIdxStr = repository.getSetting("profile_avatar_index")
        val tfaSecret = repository.getSetting("profile_2fa_secret")
        val bioEnabledStr = repository.getSetting("profile_biometric_enabled")

        userAvatarIndex.value = avatarIdxStr?.toIntOrNull() ?: 0
        user2FaSecret.value = tfaSecret
        isBiometricEnabled.value = bioEnabledStr == "true"

        if (name.isNullOrEmpty()) {
            name = generateRandomPseudonym()
            val randomColor = generateRandomColor()
            repository.saveSetting("profile_pseudonym", name)
            repository.saveSetting("profile_color", randomColor.toString())
            repository.saveSetting("profile_avatar_index", "0")
            userPseudonym.value = name
            userAvatarColor.value = randomColor
        } else {
            userPseudonym.value = name
            userAvatarColor.value = colorStr?.toIntOrNull() ?: 0xFF00796B.toInt()
        }
    }

    fun regenerateProfile() {
        viewModelScope.launch {
            val name = generateRandomPseudonym()
            val color = generateRandomColor()
            repository.saveSetting("profile_pseudonym", name)
            repository.saveSetting("profile_color", color.toString())
            userPseudonym.value = name
            userAvatarColor.value = color
        }
    }

    fun setupPasscode(pin: String) {
        viewModelScope.launch {
            if (pin.length == 4) {
                val salt = getOrCreateCryptoSalt()
                val hashedPin = IntegrityGuard.hashPasscode(pin, salt)
                repository.saveSetting("user_pin", hashedPin)
                isPasscodeSetup.value = true
                isAuthenticated.value = true
                currentScreen.value = Screen.Feed
                passcodeError.value = null
            } else {
                passcodeError.value = "PIN must be exactly 4 digits."
            }
        }
    }

    fun completeProfileSetup(
        username: String,
        pin: String,
        mobileNumber: String,
        avatarIndex: Int,
        tfaSecret: String,
        biometricEnabled: Boolean
    ) {
        viewModelScope.launch {
            val salt = getOrCreateCryptoSalt()
            val hashedPin = IntegrityGuard.hashPasscode(pin, salt)
            
            // Generate standard dynamic color corresponding to chosen avatar index
            val presetColors = listOf(
                0xFF009688.toInt(), // Cosmic Teal
                0xFFFF5722.toInt(), // Neon Sunset
                0xFF673AB7.toInt(), // Nebula Spark
                0xFF4CAF50.toInt(), // Emerald Shield
                0xFFE91E63.toInt(), // Cyberpunk Crimson
                0xFFFFC107.toInt()  // Electric Amber
            )
            val selectedColor = presetColors.getOrElse(avatarIndex) { 0xFF009688.toInt() }

            // Persist parameters
            repository.saveSetting("user_pin", hashedPin)
            repository.saveSetting("profile_pseudonym", username)
            repository.saveSetting("profile_color", selectedColor.toString())
            repository.saveSetting("profile_avatar_index", avatarIndex.toString())
            repository.saveSetting("profile_2fa_secret", tfaSecret)
            repository.saveSetting("profile_biometric_enabled", biometricEnabled.toString())
            repository.saveSetting("USER_MOBILE_NUMBER", mobileNumber)

            // Update state Flows
            userPseudonym.value = username
            userAvatarColor.value = selectedColor
            userAvatarIndex.value = avatarIndex
            user2FaSecret.value = tfaSecret
            isBiometricEnabled.value = biometricEnabled
            userMobileNumber.value = mobileNumber

            isPasscodeSetup.value = true
            isAuthenticated.value = true
            currentScreen.value = Screen.Feed
        }
    }

    fun verifyPasscode(pin: String) {
        viewModelScope.launch {
            val savedPin = repository.getSetting("user_pin")
            val salt = getOrCreateCryptoSalt()
            val hashedInput = IntegrityGuard.hashPasscode(pin, salt)
            
            // Compatible validation allowing backward fallback and dynamic upgrade
            if (hashedInput == savedPin || pin == savedPin) {
                if (pin == savedPin) {
                    repository.saveSetting("user_pin", hashedInput) // Upgrade to SHA-256 seamlessly
                }
                isAuthenticated.value = true
                currentScreen.value = Screen.Feed
                passcodeError.value = null
                passcodeText.value = ""
            } else {
                passcodeError.value = "Incorrect passcode. Please try again."
                passcodeText.value = ""
            }
        }
    }

    fun triggerSimulatedBiometricSuccess() {
        viewModelScope.launch {
            isAuthenticated.value = true
            currentScreen.value = Screen.Feed
            passcodeError.value = null
            passcodeText.value = ""
        }
    }

    // Feeling Posting with Gemini Content Moderation
    fun submitFeeling() {
        val content = postText.value.trim()
        val tag = postSelectedTag.value
        if (content.isEmpty()) return

        viewModelScope.launch {
            moderationStatus.value = ModerationState.Analyzing
            
            // Invoke Gemini Content Moderation
            val result = GeminiModerator.moderateContent(content)
            
            if (result.isSafe) {
                moderationStatus.value = ModerationState.Approved("Content is safe and secure. Posted successfully!")
                
                // Insert post into Room
                repository.insertFeelingPost(
                    FeelingPost(
                        content = content,
                        emotionTag = tag,
                        authorName = userPseudonym.value,
                        authorAvatarColor = userAvatarColor.value
                    )
                )
                
                // Clear post input fields
                postText.value = ""
                postSelectedTag.value = "Venting"
                
                // Switch back to Feed screen
                currentScreen.value = Screen.Feed
                moderationStatus.value = ModerationState.Idle
            } else {
                moderationStatus.value = ModerationState.Blocked(result.flagReason)
            }
        }
    }

    // 1-on-1 Chats Management
    fun startAnonymousChat(post: FeelingPost) {
        if (post.authorName == userPseudonym.value) {
            // Can't chat with yourself
            return
        }

        viewModelScope.launch {
            // Generate deterministic chatId based on sorted pseudonyms to ensure strict 1-on-1 channel mapping
            val sortedNames = listOf(userPseudonym.value, post.authorName).sorted()
            val chatId = "chat_${sortedNames[0]}_${sortedNames[1]}".replace(" ", "_").lowercase()

            val existingRoom = repository.getChatRoomById(chatId)
            if (existingRoom == null) {
                repository.createChatRoom(
                    ChatRoom(
                        chatId = chatId,
                        participantName = post.authorName,
                        participantAvatarColor = post.authorAvatarColor,
                        lastMessage = "Connected regarding: \"${post.content.take(30)}...\"",
                        lastMessageTimestamp = System.currentTimeMillis()
                    )
                )
            }
            
            activeChatId.value = chatId
            currentScreen.value = Screen.ChatRoomScreen(chatId)
        }
    }

    fun openChatRoom(room: ChatRoom) {
        activeChatId.value = room.chatId
        currentScreen.value = Screen.ChatRoomScreen(room.chatId)
    }

    fun sendSecureChatMessage(text: String) {
        val chatId = activeChatId.value ?: return
        val cleartext = text.trim()
        if (cleartext.isEmpty()) return

        viewModelScope.launch {
            // Apply Content Moderation before sending
            chatModerationStatus.value = ModerationState.Analyzing
            val moderationResult = GeminiModerator.moderateContent(cleartext)
            if (moderationResult.isSafe) {
                chatModerationStatus.value = ModerationState.Approved("Message sent.")
                repository.sendSecureMessage(
                    chatId = chatId,
                    senderName = userPseudonym.value,
                    cleartext = cleartext,
                    ttlMillis = messageTtl.value
                )
                // Reset status after a delay
                kotlinx.coroutines.delay(2000)
                chatModerationStatus.value = ModerationState.Idle
            } else {
                chatModerationStatus.value = ModerationState.Blocked(moderationResult.flagReason)
            }
        }
    }

    fun toggleMessageEncryptionDetails(messageId: Long) {
        val currentMap = expandedMessageEncryptionState.value.toMutableMap()
        currentMap[messageId] = !(currentMap[messageId] ?: false)
        expandedMessageEncryptionState.value = currentMap
    }

    fun setPeerPublicKey(key: String) {
        peerPublicKey.value = key
        sharedSecret.value = null // reset secret
    }

    fun verifyPeerKey() {
        val peerKey = peerPublicKey.value ?: return
        sharedSecret.value = MockEncryptionEngine.generateSharedSecret(peerKey)
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun generateDownloadLink() {
        downloadLink.value = "https://secure-data.com/download/" + UUID.randomUUID().toString() + "?expires=" + (System.currentTimeMillis() + 3600000)
    }

    fun generateMobileNumber() {
        viewModelScope.launch {
            // Generate a random 10-digit number starting with 6-9
            val firstDigit = (6..9).random().toString()
            val remainingDigits = (100000000L..999999999L).random().toString()
            val newNumber = firstDigit + remainingDigits
            userMobileNumber.value = newNumber
            repository.saveSetting("USER_MOBILE_NUMBER", newNumber)
        }
    }

    fun setMobileNumber(number: String) {
        viewModelScope.launch {
            if (number.length == 10 && number.all { it.isDigit() }) {
                userMobileNumber.value = number
                repository.saveSetting("USER_MOBILE_NUMBER", number)
            }
        }
    }

    fun startChatWithContact(contact: ContactEntity) {
        val myNumber = userMobileNumber.value ?: return
        viewModelScope.launch {
            // Sort mobile numbers to create a unique and deterministic chatId
            val sortedNumbers = listOf(myNumber, contact.mobileNumber).sorted()
            val chatId = "chat_mob_${sortedNumbers[0]}_${sortedNumbers[1]}"
            
            val existingRoom = repository.getChatRoomById(chatId)
            if (existingRoom == null) {
                // Generate a random avatar color for the chat room
                val randomColor = generateRandomColor()
                repository.createChatRoom(
                    ChatRoom(
                        chatId = chatId,
                        participantName = contact.name,
                        participantAvatarColor = randomColor,
                        lastMessage = "Started secure chat via mobile connection.",
                        lastMessageTimestamp = System.currentTimeMillis()
                    )
                )
            }
            
            activeChatId.value = chatId
            currentScreen.value = Screen.ChatRoomScreen(chatId)
        }
    }

    fun addContact(number: String, name: String) {
        viewModelScope.launch {
            val randomColor = generateRandomColor()
            repository.insertContact(ContactEntity(number, name, randomColor))
        }
    }

    fun updateInteractionTime() {
        lastInteractionTime.value = System.currentTimeMillis()
    }

    private fun lockApp() {
        isAuthenticated.value = false
        isLocked.value = true
        currentScreen.value = Screen.Auth
    }

    fun unlockApp() {
        isLocked.value = false
        isAuthenticated.value = true
        updateInteractionTime()
    }

    // Helper generators
    private fun generateRandomPseudonym(): String {
        val adjectives = listOf(
            "Silent", "Misty", "Glowing", "Gentle", "Warm", "Cosmic", "Lunar", "Solar",
            "Deep", "Luminous", "Calm", "Ethereal", "Humble", "Vibrant", "Restless", "Peaceful"
        )
        val nouns = listOf(
            "Zenith", "Cloud", "River", "Echo", "Wave", "Anchor", "Breeze", "Heart",
            "Hearth", "Gazer", "Stardust", "Pebble", "Meadow", "Whisper", "Canyon", "Beacon"
        )
        val adj = adjectives[Random.nextInt(adjectives.size)]
        val noun = nouns[Random.nextInt(nouns.size)]
        return "$adj $noun"
    }

    private fun generateRandomColor(): Int {
        val colors = listOf(
            0xFF009688, // Teal
            0xFF2196F3, // Blue
            0xFFE91E63, // Pink/Rose
            0xFF9C27B0, // Purple
            0xFF4CAF50, // Green
            0xFFFF9800, // Orange
            0xFF00BCD4, // Cyan
            0xFF673AB7  // Deep Purple
        )
        return colors[Random.nextInt(colors.size)].toInt()
    }

    private suspend fun prepopulateDemoDataIfNeeded() {
        val posts = repository.allFeelingPosts.first()
        if (posts.isEmpty()) {
            // Prep some highly empathetic support posts
            val initialDemoPosts = listOf(
                FeelingPost(
                    content = "Tired of feeling like I have to put on a mask for everyone. Just wanted to scream into the void here. It's tough trying to pretend everything is perfect.",
                    emotionTag = "Isolation",
                    authorName = "Misty River",
                    authorAvatarColor = 0xFF2196F3.toInt(),
                    timestamp = System.currentTimeMillis() - 3600000 * 3
                ),
                FeelingPost(
                    content = "Today I finally took a deep breath and took a walk. The anxiety was heavy in my chest all morning, but taking it one step at a time actually helped. Sending strength to anyone else struggling right now.",
                    emotionTag = "Anxiety",
                    authorName = "Gentle Pebble",
                    authorAvatarColor = 0xFF009688.toInt(),
                    timestamp = System.currentTimeMillis() - 3600000 * 2
                ),
                FeelingPost(
                    content = "I feel so lonely in a crowded room. My friends are laughing and talking, but I feel miles away. Is it normal to feel this disconnected?",
                    emotionTag = "Loneliness",
                    authorName = "Deep Echo",
                    authorAvatarColor = 0xFF9C27B0.toInt(),
                    timestamp = System.currentTimeMillis() - 1800000
                ),
                FeelingPost(
                    content = "Just vented my frustration to the AI moderator and rewrite it, feeling a bit lighter now. Highly recommend writing things down when your mind is racing.",
                    emotionTag = "Venting",
                    authorName = "Cosmic Hearth",
                    authorAvatarColor = 0xFFFF9800.toInt(),
                    timestamp = System.currentTimeMillis() - 600000
                )
            )
            for (p in initialDemoPosts) {
                repository.insertFeelingPost(p)
            }
        }
    }
}

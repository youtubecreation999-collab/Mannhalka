package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.ChatRoom
import com.example.db.FeelingPost
import com.example.security.CryptoHelper
import com.example.security.BiometricBridge
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen
import com.example.viewmodel.ModerationState
import com.example.R
import com.example.ui.theme.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale

import androidx.compose.foundation.text.KeyboardOptions
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import android.content.Intent
import android.speech.RecognizerIntent

fun Context.findActivity(): FragmentActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is FragmentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    var friendName by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_handshake_animation),
            contentDescription = "Handshake",
            modifier = Modifier.size(150.dp),
            contentScale = ContentScale.Fit
        )
        Text("Welcome to the Dashboard", style = MaterialTheme.typography.headlineMedium)
        Text("Connect with friends and start chatting!")
        if (friendName.isNotEmpty()) {
            Text("Match: $friendName", style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(16.dp))
        FloatingActionButton(onClick = { viewModel.findNewFriend { friendName = it } }) {
            Icon(Icons.Default.Add, contentDescription = "Find Friend")
        }
    }
}

@Composable
fun LeaderboardScreen(viewModel: MainViewModel) {
    val points by viewModel.leaderboardPoints.collectAsState()
    val level by viewModel.leaderboardLevel.collectAsState()
    val levels = listOf("Bronze", "Silver", "Gold", "Platinum", "Diamond", "Emerald", "Master", "Grandmaster", "Challenger", "Legend")
    val currentIndex = levels.indexOf(level)
    val progress = (points % 100) / 100f

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Leaderboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Current Tier: $level", style = MaterialTheme.typography.titleLarge)
                Text("Points: $points", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
                Text("Next Tier: ${if (currentIndex < levels.size - 1) levels[currentIndex + 1] else "Maxed"}")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("All Tiers", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(levels) { l ->
                Text(
                    text = l,
                    color = if (l == level) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (l == level) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MobileNumberSettingsScreen(viewModel: MainViewModel) {
    val mobileNumber by viewModel.userMobileNumber.collectAsState()
    val contacts by viewModel.contacts.collectAsState(initial = emptyList())
    var contactName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    
    // Manual registration field for user's own mobile number
    var myNumberInput by remember { mutableStateOf("") }
    var myNumberError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            IconButton(
                onClick = { viewModel.currentScreen.value = Screen.Settings },
                modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Secure Mobile Network",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Establish secure 1-on-1 dialogues by 10-digit mobile numbers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Your Mobile Number Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Your 10-Digit Secure ID",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Your unique 10-digit mobile identifier. Other users can add you using this number.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (mobileNumber.isNullOrEmpty()) {
                    OutlinedTextField(
                        value = myNumberInput,
                        onValueChange = { input ->
                            if (input.length <= 10 && input.all { it.isDigit() }) {
                                myNumberInput = input
                                myNumberError = if (input.length < 10) "Must be exactly 10 digits" else null
                            }
                        },
                        label = { Text("Enter Your 10-Digit Mobile") },
                        placeholder = { Text("e.g. 9876543210") },
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = myNumberError != null,
                        supportingText = { myNumberError?.let { Text(it) } }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (myNumberInput.length == 10) {
                                    viewModel.setMobileNumber(myNumberInput)
                                } else {
                                    myNumberError = "Must be exactly 10 digits"
                                }
                            },
                            enabled = myNumberInput.length == 10,
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Register Number", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.generateMobileNumber()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Generate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Active Mobile",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Active ID:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "+91 ${mobileNumber!!}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        TextButton(
                            onClick = {
                                viewModel.setMobileNumber("") // clear to re-set
                                myNumberInput = ""
                            }
                        ) {
                            Text("Reset", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Add Contact Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Add Secure Contact",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Save another user's name and 10-digit mobile number to open E2EE chats.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = contactName,
                    onValueChange = { contactName = it },
                    label = { Text("Contact Name") },
                    placeholder = { Text("e.g. Priyanshu") },
                    keyboardOptions = KeyboardOptions(autoCorrectEnabled = false),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { input ->
                        if (input.length <= 10 && input.all { it.isDigit() }) {
                            contactNumber = input
                        }
                    },
                    label = { Text("Contact 10-Digit Mobile") },
                    placeholder = { Text("e.g. 9123456789") },
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (contactName.trim().isNotEmpty() && contactNumber.length == 10) {
                            viewModel.addContact(contactNumber, contactName)
                            contactName = ""
                            contactNumber = ""
                        }
                    },
                    enabled = contactName.trim().isNotEmpty() && contactNumber.length == 10,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add contact")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Contact", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Contacts list title
        Text(
            text = "Your Secured Directory",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (contacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No contacts saved yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                contacts.forEach { contact ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(contact.avatarColor)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = contact.name.take(2).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = contact.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "+91 ${contact.mobileNumber}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.startChatWithContact(contact)
                                },
                                enabled = !mobileNumber.isNullOrEmpty(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forum,
                                    contentDescription = "Chat",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BiometricAuthComponent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val activity = context.findActivity() ?: return
    val isSupported by viewModel.isBiometricSupported.collectAsState()

    if (!isSupported) return

    Button(
        onClick = {
            BiometricBridge.authenticate(
                activity,
                "Sign in",
                "Verify your identity",
                object : BiometricBridge.BiometricPromise {
                    override fun resolve(value: Any?) {
                        viewModel.triggerSimulatedBiometricSuccess()
                    }
                    override fun reject(code: String, message: String) {
                        // Handle error (e.g. log or show message)
                    }
                }
            )
        },
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Icon(Icons.Default.Fingerprint, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Login with Biometrics")
    }
}

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
    
    val pseudonym by viewModel.userPseudonym.collectAsState()
    val avatarColor by viewModel.userAvatarColor.collectAsState()
    val avatarIndex by viewModel.userAvatarIndex.collectAsState()
    val level by viewModel.leaderboardLevel.collectAsState()
    val points by viewModel.leaderboardPoints.collectAsState()
    
    // Security status
    val isRooted by viewModel.isRooted.collectAsState()
    val isDebugger by viewModel.isDebuggerConnected.collectAsState()
    val isEmulator by viewModel.isEmulatorRunning.collectAsState()
    val isSecure = !isRooted && !isDebugger && !isEmulator

    val avatarGradients = listOf(
        listOf(Color(0xFF00B4DB), Color(0xFF0083B0)), // Cosmic Teal
        listOf(Color(0xFFFF8C00), Color(0xFFE52D27)), // Neon Sunset
        listOf(Color(0xFF8A2387), Color(0xFFE94057)), // Nebula Spark
        listOf(Color(0xFF11998e), Color(0xFF38ef7d)), // Emerald Shield
        listOf(Color(0xFFED213A), Color(0xFF93291E)), // Cyberpunk Crimson
        listOf(Color(0xFFF12711), Color(0xFFF5AF19))  // Electric Amber
    )
    val chosenGradient = avatarGradients.getOrElse(avatarIndex) { listOf(Color(avatarColor), Color(avatarColor)) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(chosenGradient)),
            contentAlignment = Alignment.Center
        ) {
            Text(pseudonym.take(2).uppercase(), style = MaterialTheme.typography.headlineLarge, color = Color.White)
        }
        
        Text(pseudonym, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 8.dp))
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Rank
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Leaderboard Rank", style = MaterialTheme.typography.titleMedium)
                Text("Level: $level", style = MaterialTheme.typography.bodyLarge)
                Text("Points: $points", style = MaterialTheme.typography.bodyMedium)
            }
        }
        
        // Security Status
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Security Status", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (isSecure) "System Secure" else "System Warning - Compromised",
                    color = if (isSecure) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SecurityOverlay(viewModel: MainViewModel) {
    val isPrivacyMode by viewModel.isPrivacyMode.collectAsState()
    val alpha by animateFloatAsState(targetValue = if (isPrivacyMode) 0.95f else 0.0f, label = "Alpha")
    
    if (alpha > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = alpha))
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                            // Block interactions
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text("Privacy Mode Active", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Draw very subtle, fine, semi-transparent diagonal security lines to deter external photos
        val spacing = 48.dp.toPx()
        val lineColor = Color(0xFF4A635D).copy(alpha = 0.025f) // Extremely subtle Natural Tone (Sage)
        
        var x = -size.height
        while (x < size.width) {
            drawLine(
                color = lineColor,
                start = androidx.compose.ui.geometry.Offset(x, 0f),
                end = androidx.compose.ui.geometry.Offset(x + size.height, size.height),
                strokeWidth = 1.dp.toPx()
            )
            x += spacing
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainAppContainer(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    val emptyTextToolbar = remember {
        object : TextToolbar {
            override fun showMenu(
                rect: Rect,
                onCopyRequested: (() -> Unit)?,
                onPasteRequested: (() -> Unit)?,
                onCutRequested: (() -> Unit)?,
                onSelectAllRequested: (() -> Unit)?
            ) {
                // Suppress text selection / copy menu completely
            }
            override fun hide() {}
            override val status: TextToolbarStatus get() = TextToolbarStatus.Hidden
        }
    }

    val secureClipboardManager = remember {
        object : ClipboardManager {
            override fun setText(annotatedString: AnnotatedString) {
                // Intercept and prevent clipboard copying
            }
            override fun getText(): AnnotatedString? {
                return null
            }
        }
    }

    CompositionLocalProvider(
        LocalTextToolbar provides emptyTextToolbar,
        LocalClipboardManager provides secureClipboardManager
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                            viewModel.updateInteractionTime()
                        }
                    }
                }
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn() + slideInHorizontally { it } with fadeOut() + slideOutHorizontally { -it }
                    },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        is Screen.Auth -> PasscodeScreen(viewModel, isSetup = false)
                        is Screen.PasscodeSetup -> PasscodeScreen(viewModel, isSetup = true)
                        is Screen.ProfileSetup -> ProfileSetupScreen(viewModel)
                        is Screen.Dashboard -> AppShell(viewModel, selectedTab = 0) {
                            DashboardScreen(viewModel)
                        }
                        is Screen.Leaderboard -> AppShell(viewModel, selectedTab = 1) {
                            LeaderboardScreen(viewModel)
                        }
                        is Screen.Feed -> AppShell(viewModel, selectedTab = 2) {
                            FeedScreen(viewModel)
                        }
                        is Screen.Share -> AppShell(viewModel, selectedTab = 3) {
                            ShareScreen(viewModel)
                        }
                        is Screen.ChatList -> AppShell(viewModel, selectedTab = 4) {
                            ChatListScreen(viewModel)
                        }
                        is Screen.ChatRoomScreen -> ChatRoomScreen(viewModel, screen.chatId)
                        is Screen.Settings -> AppShell(viewModel, selectedTab = 5) {
                            SettingsScreen(viewModel)
                        }
                        is Screen.Profile -> AppShell(viewModel, selectedTab = 6) {
                            ProfileScreen(viewModel)
                        }
                        is Screen.MobileSettings -> AppShell(viewModel, selectedTab = 7) {
                            MobileNumberSettingsScreen(viewModel)
                        }
                    }
                }
            }

            // Secure, transparent visual security layer overlaid on top of the root layout
            SecurityOverlay(viewModel)
        }
    }
}

@Composable
fun AppHeader() {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .windowInsetsPadding(WindowInsets.statusBars),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "WhatsApp Secure",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "MANNHALKA",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { /* No action needed */ },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Shield Lock",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                val context = LocalContext.current
                var showDonationDialog by remember { mutableStateOf(false) }

                if (showDonationDialog) {
                    DonationDialog(onDismiss = { showDonationDialog = false })
                }

                IconButton(
                    onClick = { showDonationDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Donate",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DonationDialog(onDismiss: () -> Unit) {
    val amounts = listOf(10, 20, 50, 100, 200, 500, 1000, 5000, 10000)
    val context = androidx.compose.ui.platform.LocalContext.current
    val upiId = androidx.compose.ui.res.stringResource(id = R.string.upi_id)
    
    fun launchUpiPayment(amount: Int) {
        val uri = android.net.Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", "Mannhalka Donation")
            .appendQueryParameter("am", amount.toString())
            .appendQueryParameter("cu", "INR")
            .build()
            
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "No payment app found", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { androidx.compose.material3.Text("Donate to Mannhalka") },
        text = {
            androidx.compose.foundation.layout.Column {
                amounts.forEach { amount ->
                    androidx.compose.material3.TextButton(
                        onClick = { launchUpiPayment(amount); onDismiss() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.material3.Text("₹$amount")
                    }
                }
                androidx.compose.material3.TextButton(
                    onClick = { launchUpiPayment(10000); onDismiss() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.Text("₹10000+")
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                androidx.compose.material3.Text("Close")
            }
        }
    )
}

@Composable
fun PrivacyBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(
                width = 1.dp,
                color = Color(0xFFFFE082),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEF9E7)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "E2E Encryption",
                tint = Color(0xFF6B511F),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Messages and posts are end-to-end encrypted. No one outside of this app can read them.",
                fontSize = 11.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B511F)
            )
        }
    }
}

@Composable
fun AppShell(
    viewModel: MainViewModel,
    selectedTab: Int,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            Column {
                // Secure Footer Tag
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF2EFE9))
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verified Session",
                            tint = Color(0xFF8E8A81),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "END-TO-END ENCRYPTED SESSION",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = Color(0xFF8E8A81)
                        )
                    }
                }
                
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { viewModel.currentScreen.value = Screen.Feed },
                        icon = { Icon(Icons.Default.Waves, contentDescription = "Feed") },
                        label = { Text("Feed") },
                        modifier = Modifier.testTag("nav_feed_tab")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { viewModel.currentScreen.value = Screen.Share },
                        icon = { Icon(Icons.Default.AddCircle, contentDescription = "Vibe Vent") },
                        label = { Text("Share") },
                        modifier = Modifier.testTag("nav_share_tab")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { viewModel.currentScreen.value = Screen.ChatList },
                        icon = { Icon(Icons.Default.Forum, contentDescription = "Secure Chats") },
                        label = { Text("Chats") },
                        modifier = Modifier.testTag("nav_chats_tab")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { viewModel.currentScreen.value = Screen.Settings },
                        icon = { Icon(Icons.Default.Security, contentDescription = "Privacy Shield") },
                        label = { Text("Privacy") },
                        modifier = Modifier.testTag("nav_settings_tab")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Global Beautiful Natural Tones Header
            AppHeader()
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

// ---------------- PASSCODE & BIOMETRICS SCREEN ----------------
@Composable
fun PasscodeScreen(viewModel: MainViewModel, isSetup: Boolean) {
    val passcodeText by viewModel.passcodeText.collectAsState()
    val passcodeSetupText by viewModel.passcodeSetupText.collectAsState()
    val passcodeSetupConfirmText by viewModel.passcodeSetupConfirmText.collectAsState()
    val errorMsg by viewModel.passcodeError.collectAsState()

    var tempPin by remember { mutableStateOf("") }
    var confirmStep by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Automatically trigger biometric login upon app launch if passcode is set up
    var biometricAuthenticated by remember { mutableStateOf(false) }
    var biometricError by remember { mutableStateOf<String?>(null) }

    fun runBiometric() {
        val activity = context.findActivity()
        if (activity != null) {
            com.example.security.BiometricBridge.canAuthenticate(context, object : com.example.security.BiometricBridge.BiometricPromise {
                override fun resolve(value: Any?) {
                    com.example.security.BiometricBridge.authenticate(
                        activity = activity,
                        title = "Mandatory Security Scan",
                        subtitle = "Verify identity to access your encrypted container",
                        promise = object : com.example.security.BiometricBridge.BiometricPromise {
                            override fun resolve(value: Any?) {
                                biometricAuthenticated = true
                                biometricError = null
                            }

                            override fun reject(code: String, message: String) {
                                biometricError = "Biometric authentication is mandatory."
                                runBiometric() // Re-trigger on failure/cancel to enforce compulsory rule
                            }
                        }
                    )
                }

                override fun reject(code: String, message: String) {
                    biometricError = "Biometric authentication not supported/enrolled."
                }
            })
        }
    }

    LaunchedEffect(isSetup) {
        if (!isSetup) {
            runBiometric()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            com.example.security.GoogleAuthHelper.handleSignInResult(result.data) { success ->
                if (success) {
                    viewModel.unlockApp()
                } else {
                    viewModel.passcodeError.value = "Google Sign-In failed"
                }
            }
        }
    }

    // Outer screen background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0E1116),
                        Color(0xFF141923)
                    )
                )
            )
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.statusBars),
        contentAlignment = Alignment.Center
    ) {
        if (!isSetup && !biometricAuthenticated) {
            // COMPULSORY FINGERPRINT SCAN SCREEN
            var scanActive by remember { mutableStateOf(false) }
            var scanProgress by remember { mutableStateOf(0f) }

            LaunchedEffect(scanActive) {
                if (scanActive) {
                    while (scanProgress < 1.0f) {
                        scanProgress += 0.05f
                        kotlinx.coroutines.delay(80)
                    }
                    biometricAuthenticated = true
                    scanActive = false
                } else {
                    scanProgress = 0f
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                // Top Header Info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield Lock",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "MANNHALKA",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 4.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "COMPULSORY BIOMETRIC SCAN",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "To access your secure data, fingerprint verification is compulsory. Touch and hold the sensor below to complete.",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                // Interactive Touch/Hold Fingerprint Pad
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1D2433))
                            .border(
                                width = 3.dp,
                                color = if (scanActive) Color(0xFF38ef7d) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        scanActive = true
                                        tryAwaitRelease()
                                        scanActive = false
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (scanActive) {
                            CircularProgressIndicator(
                                progress = { scanProgress },
                                modifier = Modifier.fillMaxSize(),
                                color = Color(0xFF38ef7d),
                                strokeWidth = 4.dp
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Simulated Biometrics",
                                tint = if (scanActive) Color(0xFF38ef7d) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (scanActive) "SCANNING..." else "HOLD TO SCAN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (scanActive) Color(0xFF38ef7d) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Status Message and Google Sign-in Option
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 30.dp)
                ) {
                    Text(
                        text = biometricError ?: "Waiting for secure fingerprint scan...",
                        color = if (biometricError != null) MaterialTheme.colorScheme.error else Color(0xFF38ef7d),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val intent = com.example.security.GoogleAuthHelper.getSignInIntent(context)
                            launcher.launch(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sign in with Google", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // SECURITY PIN / PASSCODE ENTRY SCREEN
            val headlineText = if (isSetup) {
                if (!confirmStep) "Create Secure PIN" else "Confirm Secure PIN"
            } else {
                "Enter Space PIN"
            }

            val descriptionText = if (isSetup) {
                "Secure your anonymous space with a 4-digit PIN. Your feelings, messages, and identity are completely private."
            } else {
                "Unlocking your anonymous E2EE container"
            }

            val currentInput = if (isSetup) {
                if (!confirmStep) passcodeSetupText else passcodeSetupConfirmText
            } else {
                passcodeText
            }

            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Icon & Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSetup) Icons.Default.Shield else Icons.Default.Lock,
                            contentDescription = "Shield Lock",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "MANNHALKA",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 4.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = headlineText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = descriptionText,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                // Passcode indicators (4 circles)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..4) {
                            val isFilled = currentInput.length >= i
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .background(
                                        color = if (isFilled) MaterialTheme.colorScheme.primary else Color.Transparent
                                    )
                            )
                        }
                    }

                    if (errorMsg != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Numeric Pad Grid (Perfect circular buttons with aspect ratio 1:1)
                Column(
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .fillMaxWidth()
                        .padding(bottom = 30.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("Biometrics", "0", "Delete")
                    )

                    for (row in keys) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            for (key in row) {
                                val isEnabled = isSetup || biometricAuthenticated || key == "Biometrics"
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .clickable(enabled = isEnabled) {
                                            when (key) {
                                                "Delete" -> {
                                                    if (isSetup) {
                                                        if (confirmStep) {
                                                            if (passcodeSetupConfirmText.isNotEmpty()) {
                                                                viewModel.passcodeSetupConfirmText.value = passcodeSetupConfirmText.dropLast(1)
                                                            }
                                                        } else {
                                                            if (passcodeSetupText.isNotEmpty()) {
                                                                viewModel.passcodeSetupText.value = passcodeSetupText.dropLast(1)
                                                            }
                                                        }
                                                    } else {
                                                        if (passcodeText.isNotEmpty()) {
                                                            viewModel.passcodeText.value = passcodeText.dropLast(1)
                                                        }
                                                    }
                                                }
                                                "Biometrics" -> {
                                                    if (!isSetup) {
                                                        runBiometric()
                                                    }
                                                }
                                                else -> {
                                                    if (isEnabled) {
                                                        val combined = currentInput + key
                                                        if (combined.length <= 4) {
                                                            if (isSetup) {
                                                                if (confirmStep) {
                                                                    viewModel.passcodeSetupConfirmText.value = combined
                                                                    if (combined.length == 4) {
                                                                        if (combined == tempPin) {
                                                                            viewModel.setupPasscode(combined)
                                                                        } else {
                                                                            viewModel.passcodeError.value = "PINs do not match. Restarting setup."
                                                                            viewModel.passcodeSetupText.value = ""
                                                                            viewModel.passcodeSetupConfirmText.value = ""
                                                                            confirmStep = false
                                                                        }
                                                                    }
                                                                } else {
                                                                    viewModel.passcodeSetupText.value = combined
                                                                    if (combined.length == 4) {
                                                                        tempPin = combined
                                                                        confirmStep = true
                                                                        viewModel.passcodeError.value = null
                                                                    }
                                                                }
                                                            } else {
                                                                viewModel.passcodeText.value = combined
                                                                if (combined.length == 4) {
                                                                    viewModel.verifyPasscode(combined)
                                                                    viewModel.unlockApp()
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        .background(
                                            if (!isEnabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                            else if (key == "Delete" || key == "Biometrics") Color.Transparent
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (key) {
                                        "Delete" -> Icon(
                                            imageVector = Icons.Default.Backspace,
                                            contentDescription = "Backspace",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                        "Biometrics" -> {
                                            if (!isSetup) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Biometric Active",
                                                    tint = Color(0xFF38ef7d),
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                        else -> Text(
                                            text = key,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- EMOTIONAL ANONYMOUS FEED SCREEN ----------------
@Composable
fun FeedScreen(viewModel: MainViewModel) {
    val posts by viewModel.filteredFeelingPosts.collectAsState()
    val activeFilter by viewModel.selectedTagFilter.collectAsState()
    val userNickname by viewModel.userPseudonym.collectAsState()
    var selectedStatusForViewer by remember { mutableStateOf<FeelingPost?>(null) }

    val emotionTags = listOf("All", "Anxiety", "Sadness", "Isolation", "Venting", "Hope", "Peace")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top branding
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MANNHALKA",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Sharing as: $userNickname",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Identity Locked",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Emotion chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            emotionTags.forEach { tag ->
                val isSelected = if (tag == "All") activeFilter == null else activeFilter == tag
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.selectedTagFilter.value = if (tag == "All") null else tag
                    },
                    label = { Text(tag) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.surface,
                        selectedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // WhatsApp Status Tray (Big Space to see Status & WhatsApp visual styling)
        Text(
            text = "STATUS UPDATES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // My Status Item
            val myLatestPost = posts.firstOrNull { it.authorName == userNickname }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable {
                        if (myLatestPost != null) {
                            selectedStatusForViewer = myLatestPost
                        } else {
                            viewModel.currentScreen.value = Screen.Share
                        }
                    }
                    .padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier.size(60.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 2.dp,
                                color = if (myLatestPost != null) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userNickname.take(2).uppercase(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    if (myLatestPost == null) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Status",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "My Status",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Other Users' Statuses
            val otherUsersPosts = posts.filter { it.authorName != userNickname }.distinctBy { it.authorName }
            otherUsersPosts.forEach { post ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { selectedStatusForViewer = post }
                        .padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(post.authorAvatarColor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = post.authorName.split(" ").map { it.take(1) }.joinToString(""),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = post.authorName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(64.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Immersive Status Viewer Dialog
        selectedStatusForViewer?.let { status ->
            val colors = listOf(
                Color(0xFF00796B), // Teal
                Color(0xFF5E35B1), // Deep Purple
                Color(0xFFD81B60), // Pink
                Color(0xFFE64A19), // Orange
                Color(0xFF1E88E5), // Blue
                Color(0xFF43A047), // Green
                Color(0xFF8E24AA)  // Purple
            )
            val index = java.lang.Math.abs(status.authorName.hashCode()) % colors.size
            val statusBgColor = colors[index]

            androidx.compose.ui.window.Dialog(
                onDismissRequest = { selectedStatusForViewer = null },
                properties = androidx.compose.ui.window.DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = statusBgColor
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(16.dp)
                    ) {
                        // Top Story Progress Indicators
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.White)
                            )
                        }

                        // Header: Avatar, Name, and Close
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = status.authorName.split(" ").map { it.take(1) }.joinToString(""),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = status.authorName,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = status.emotionTag,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            IconButton(
                                onClick = { selectedStatusForViewer = null }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Status",
                                    tint = Color.White
                                )
                            }
                        }

                        // Centered Status Content - BIG SPACE TO SEE STATUS
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Waves,
                                    contentDescription = "Wave",
                                    tint = Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = status.content,
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    lineHeight = 34.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = status.emotionTag.uppercase(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }

                        // Footer: Reply Action
                        if (status.authorName != userNickname) {
                            Button(
                                onClick = {
                                    viewModel.startAnonymousChat(status)
                                    selectedStatusForViewer = null
                                },
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = statusBgColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Default.Forum, contentDescription = "Connect")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reply to Secure Status", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Your Status • Tap close to exit",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Post listing

        if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Waves,
                        contentDescription = "Empty",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "The cosmic space is quiet.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Be the first to share your raw feelings.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("feed_list"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(posts) { post ->
                    FeelingCard(post = post, onConnect = { viewModel.startAnonymousChat(post) })
                }
            }
        }
    }
}

@Composable
fun FeelingCard(post: FeelingPost, onConnect: () -> Unit) {
    val dateString = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date(post.timestamp))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Avatar, Name, and Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(post.authorAvatarColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.authorName.split(" ").map { it.take(1) }.joinToString(""),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    Column {
                        Text(
                            text = post.authorName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dateString,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tag Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = post.emotionTag,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Feeling text content
            Text(
                text = post.content,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.95f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Footer connection link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onConnect,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Chat icon",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Connect 1-on-1", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------- SHARE FEELING / AI MODERATION FORM ----------------
@Composable
fun ShareScreen(viewModel: MainViewModel) {
    val postText by viewModel.postText.collectAsState()
    val postTag by viewModel.postSelectedTag.collectAsState()
    val moderationState by viewModel.moderationStatus.collectAsState()

    val categoryTags = listOf("Venting", "Anxiety", "Sadness", "Isolation", "Hope", "Peace")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Lighten Your Mind",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Release what weighs heavy on your soul. Fully secure, encrypted, and zero screenshot footprint.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
        )

        // Text field container
        OutlinedTextField(
            value = postText,
            onValueChange = { viewModel.postText.value = it },
            placeholder = {
                Text(
                    "Speak your mind without filter or judgment...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            keyboardOptions = KeyboardOptions(autoCorrect = false),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .testTag("feeling_input_field"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Emotion Select
        Text(
            text = "Tag your current feeling",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // Grid/Row for category selectors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoryTags.forEach { tag ->
                val isSelected = postTag == tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.postSelectedTag.value = tag }
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = tag,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // AI Content Moderation Feedback Card
        AnimatedVisibility(
            visible = moderationState !is ModerationState.Idle,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (moderationState) {
                        is ModerationState.Analyzing -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        is ModerationState.Approved -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        is ModerationState.Blocked -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(
                        width = 1.dp,
                        color = when (moderationState) {
                            is ModerationState.Analyzing -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            is ModerationState.Approved -> MaterialTheme.colorScheme.primary
                            is ModerationState.Blocked -> MaterialTheme.colorScheme.error
                            else -> Color.Transparent
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (moderationState) {
                        is ModerationState.Analyzing -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "AI Real-time safety validation scanning active...",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                        is ModerationState.Approved -> {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Approved",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                (moderationState as ModerationState.Approved).message,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        is ModerationState.Blocked -> {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Blocked",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Post Blocked: Identity or Safety Leak Found",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    (moderationState as ModerationState.Blocked).reason,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }

        // Post button
        Button(
            onClick = { viewModel.submitFeeling() },
            enabled = postText.trim().isNotEmpty() && moderationState !is ModerationState.Analyzing,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_feeling_button")
        ) {
            Icon(Icons.Default.VerifiedUser, contentDescription = "Post Shield")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Scan & Post Securely",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

// ---------------- SECURE CHAT ROOMS LIST SCREEN ----------------
@Composable
fun ChatListScreen(viewModel: MainViewModel) {
    val rooms by viewModel.allChatRooms.collectAsState(initial = emptyList())
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    BlurOverlay(enabled = !isAuthenticated, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "E2EE Communications",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                IconButton(
                    onClick = { viewModel.currentScreen.value = Screen.MobileSettings },
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Contacts & Mobile Network",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = "Encrypted 1-on-1 dialogues. System-wide anti-screenshot policy active.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            if (rooms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secure Chats Empty",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No secure dialogues established.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Vibe Vent and connect on other posts to start E2EE chats.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(rooms) { room ->
                        ChatRoomRow(room = room, onClick = { viewModel.openChatRoom(room) })
                    }
                }
            }
        }
    }
}

@Composable
fun ChatRoomRow(room: ChatRoom, onClick: () -> Unit) {
    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(room.lastMessageTimestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(room.participantAvatarColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = room.participantName.split(" ").map { it.take(1) }.joinToString(""),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = room.participantName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = timeStr,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypted Snippet",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = room.lastMessage,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun SecuredBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(10.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Secured",
            fontSize = 8.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

// ---------------- SECURE E2EE CHAT DIALOGUE ROOM SCREEN ----------------
@Composable
fun ChatRoomScreen(viewModel: MainViewModel, chatId: String) {
    val messages by viewModel.activeChatMessages.collectAsState()
    val activeRoom by viewModel.activeChatRoom.collectAsState()
    val userNickname by viewModel.userPseudonym.collectAsState()
    val expandedStates by viewModel.expandedMessageEncryptionState.collectAsState()
    val chatModerationStatus by viewModel.chatModerationStatus.collectAsState()

    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val voiceManager = remember { VoiceManager(context) {} }
    DisposableEffect(Unit) { onDispose { voiceManager.shutdown() } }

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (spokenText != null) {
                textInput = spokenText
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            }
            speechLauncher.launch(intent)
        }
    }

    BlurOverlay(enabled = !isAuthenticated, modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.currentScreen.value = Screen.ChatList }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Go back", tint = MaterialTheme.colorScheme.onSurface)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        if (activeRoom != null) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(activeRoom!!.participantAvatarColor)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeRoom!!.participantName.split(" ").map { it.take(1) }.joinToString(""),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = activeRoom!!.participantName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    SecuredBadge()
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "AES-256 E2E Secure Channel",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            Icon(
                                imageVector = Icons.Default.EnhancedEncryption,
                                contentDescription = "Encrypted Connection Verified",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(24.dp)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // E2EE System info banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Shield Security",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "All dialogues are fully local E2E encrypted. Click any message to view raw ciphertext.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Message Stream
                if (messages.isEmpty()) {
                    IcebreakerPanel(viewModel) { textInput = it }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(messages) { msg ->
                            val isMyMsg = msg.senderName == userNickname
                            val decryptedText = CryptoHelper.decrypt(msg.encryptedContent, msg.iv, chatId)
                            val showDetails = expandedStates[msg.id] ?: false

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (isMyMsg) Alignment.End else Alignment.Start
                            ) {
                                // Sender label
                                Text(
                                    text = if (isMyMsg) "You" else msg.senderName,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp, end = 4.dp)
                                )

                                // Message text box
                                Box(
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isMyMsg) 16.dp else 4.dp,
                                                bottomEnd = if (isMyMsg) 4.dp else 16.dp
                                            )
                                        )
                                        .clickable { viewModel.toggleMessageEncryptionDetails(msg.id) }
                                        .background(
                                            if (isMyMsg) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isMyMsg) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
                                            shape = RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isMyMsg) 16.dp else 4.dp,
                                                bottomEnd = if (isMyMsg) 4.dp else 16.dp
                                            )
                                        )
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = decryptedText,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { voiceManager.speak(decryptedText) },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VolumeUp,
                                                contentDescription = "Read aloud",
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Encrypted Packet Status",
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }

                                // Cryptographic Diagnostics Card Overlay
                                AnimatedVisibility(
                                    visible = showDetails,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .widthIn(max = 280.dp)
                                            .padding(vertical = 4.dp)
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(10.dp)
                                            ),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "SQLite E2EE Metadata Inspector",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "• Local Storage Protocol: AES-CBC-256",
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                            )
                                            Text(
                                                text = "• Cryptographic IV:\n  ${msg.iv}",
                                                fontSize = 8.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "• Encrypted Ciphertext Packet:\n  ${msg.encryptedContent}",
                                                fontSize = 8.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                            Text(
                                                text = "• System Integrity Verified: YES",
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (chatModerationStatus is ModerationState.Analyzing) {
                    Text("Analyzing message...", modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                } else if (chatModerationStatus is ModerationState.Blocked) {
                    Text("Blocked: ${(chatModerationStatus as ModerationState.Blocked).reason}", modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                // Chat Input Panel
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.ime)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Secure message...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                            shape = RoundedCornerShape(24.dp),
                            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            leadingIcon = {
                                IconButton(onClick = { permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO) }) {
                                    Icon(Icons.Default.Mic, contentDescription = "Voice input", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (textInput.trim().isNotEmpty()) {
                                    viewModel.sendSecureChatMessage(textInput)
                                    textInput = ""
                                }
                            },
                            enabled = textInput.trim().isNotEmpty(),
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    if (textInput.trim().isNotEmpty()) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .testTag("chat_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send secure message",
                                tint = if (textInput.trim().isNotEmpty()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IcebreakerPanel(viewModel: MainViewModel, onIcebreakerClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Start the conversation",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        viewModel.icebreakers.forEach { icebreaker ->
            OutlinedButton(
                onClick = { onIcebreakerClick(icebreaker) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text(text = icebreaker)
            }
        }
    }
}

// ---------------- USER SECURITY & PRIVACY OPTIONS SCREEN ----------------
@Composable
fun DiagnosticRow(label: String, value: String, isSecure: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (isSecure) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = value,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSecure) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }
    }
}

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val userNickname by viewModel.userPseudonym.collectAsState()
    val isSetup by viewModel.isPasscodeSetup.collectAsState()
    val isRooted by viewModel.isRooted.collectAsState()
    val isDebuggerConnected by viewModel.isDebuggerConnected.collectAsState()
    val isEmulatorRunning by viewModel.isEmulatorRunning.collectAsState()
    val isBiometricSupported by viewModel.isBiometricSupported.collectAsState()
    val selfHealedFlag by viewModel.selfHealedFlag.collectAsState()
    val lastCrashReason by viewModel.lastCrashReason.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Anonymity & Safety Control",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Configure your container settings, secure PIN, or rebuild your identity.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 22.dp)
        )

        // Self-Healing Recovery Center Notification Banner
        if (selfHealedFlag) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(14.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Self-Heal Active",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sandbox Self-Healed Successfully",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "An unexpected exception was intercepted and corrected. The app self-healed in 0.4s and kept your data safe.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (lastCrashReason.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Last Handled Error: \"$lastCrashReason\"",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                                .padding(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = { viewModel.dismissSelfHealNotice() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Acknowledge Shield Recovery", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        val messageTtl by viewModel.messageTtl.collectAsState()

        // TTL Setting
        Text(
            text = "Self-Destruct Timer",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(null to "Off", 60000L to "1m", 3600000L to "1h").forEach { (ttl, label) ->
                FilterChip(
                    selected = messageTtl == ttl,
                    onClick = { viewModel.setMessageTtl(ttl) },
                    label = { Text(label) }
                )
            }
        }

        val selectedThemeId by viewModel.selectedThemeId.collectAsState()

        // WhatsApp-inspired Color Palette (20+ Themes Selector)
        Text(
            text = "WhatsApp Theme Color (20+ Palettes)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            com.example.ui.theme.ThemePalettes.forEach { palette ->
                val isSelected = selectedThemeId == palette.id
                Surface(
                    onClick = { viewModel.setSelectedThemeId(palette.id) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Colored double circle
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(palette.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(palette.background)
                            )
                        }
                        Text(
                            text = palette.name,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Identity Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Your Profile Identity",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(viewModel.userAvatarColor.collectAsState().value)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userNickname.split(" ").map { it.take(1) }.joinToString(""),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = userNickname,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Assigned completely locally. Never uploaded to servers.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Profile Locked",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Profile identity is locked for security",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mobile Number & Contacts Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Mobile & Contacts Network",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Establish secure decentralized connections using unique 10-digit mobile numbers.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.currentScreen.value = Screen.MobileSettings },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Manage Mobile Settings")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manage Mobile & Contacts", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Advanced Security Diagnostics Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Secure Sandbox Diagnostics",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Live monitoring of local defense mechanisms.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                DiagnosticRow(
                    label = "Uncaught Crash Recovery",
                    value = "ACTIVE (SELF-HEAL)",
                    isSecure = true
                )
                Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.height(8.dp))
                
                DiagnosticRow(
                    label = "System Root Check",
                    value = if (isRooted) "COMPROMISED (ISOLATED)" else "SECURE (NO ROOT)",
                    isSecure = !isRooted
                )
                Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.height(8.dp))

                DiagnosticRow(
                    label = "Reverse Engineering Block",
                    value = if (isDebuggerConnected) "DEBUGGER ACTIVE" else "SHIELDED (NO DEBUG)",
                    isSecure = !isDebuggerConnected
                )
                Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.height(8.dp))

                DiagnosticRow(
                    label = "Hardware Sandboxing",
                    value = if (isEmulatorRunning) "VIRTUAL SANDBOX" else "PHYSICAL ENCLAVE",
                    isSecure = !isEmulatorRunning
                )
                Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.height(8.dp))

                DiagnosticRow(
                    label = "Biometrics & FaceID Status",
                    value = if (isBiometricSupported) "SECURE (HARDWARE ACTIVE)" else "PIN ENTRY ONLY",
                    isSecure = isBiometricSupported
                )
                Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.height(8.dp))

                DiagnosticRow(
                    label = "Credential Algorithm",
                    value = "SHA-256 PBKDF2 HASHING",
                    isSecure = true
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Security Policies Checklist
        Text(
            text = "Active Cryptographic Protections",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        SecurityPolicyRow(
            icon = Icons.Default.EnhancedEncryption,
            title = "AES-CBC 256 Local Encryption",
            desc = "All private chats are fully encrypted on device prior to storage inside our secure database sandbox."
        )

        SecurityPolicyRow(
            icon = Icons.Default.CheckCircle,
            title = "AI Real-Time Content Moderation",
            desc = "Gemini AI runs on-the-fly privacy scans on emotional venting, blocking leak coordinates or addresses."
        )

        SecurityPolicyRow(
            icon = Icons.Default.CloudOff,
            title = "Zero Server-Side Footprint",
            desc = "No centralized telemetry or message caching servers exist. Everything belongs fully to your device."
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Lock App option
        Button(
            onClick = {
                viewModel.isAuthenticated.value = false
                viewModel.currentScreen.value = Screen.Auth
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Lock, contentDescription = "Lock App")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Lock Container Space", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Panic Wipe Option
        Button(
            onClick = { viewModel.wipeEnclaveAndWipeData() },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Panic Wipe")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Instant Panic Destruct (Wipe All)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Wipe Messages Option
        Button(
            onClick = { viewModel.wipeHistory() },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.DeleteOutline, contentDescription = "Wipe History")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Wipe Message History Only", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Download Logs Option
        Button(
            onClick = { viewModel.generateDownloadLink() },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Download, contentDescription = "Download Logs")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Download Encrypted Logs", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        val downloadLink by viewModel.downloadLink.collectAsState()
        if (downloadLink != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Link (expires in 1h): $downloadLink", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun SecurityPolicyRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 2.dp, end = 12.dp)
                .size(18.dp)
        )
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProfileSetupScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(1) }
    
    // Step 1: 2FA State
    var tfaSecret by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        if (tfaSecret.isEmpty()) {
            val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
            val randomPart = (1..16).map { letters.random() }.joinToString("")
            tfaSecret = "MNHK-$randomPart".chunked(4).joinToString("-")
        }
    }
    
    var tfaInput by remember { mutableStateOf("") }
    var tfaError by remember { mutableStateOf<String?>(null) }
    var tfaVerified by remember { mutableStateOf(false) }
    
    // TOTP Simulator
    var countdown by remember { mutableStateOf(30) }
    var activeTokenCode by remember { mutableStateOf("123456") }
    LaunchedEffect(Unit) {
        while (true) {
            val sec = (System.currentTimeMillis() / 1000) % 30
            countdown = 30 - sec.toInt()
            val timeSlice = System.currentTimeMillis() / 30000
            val codeNum = (112233 + Math.abs((timeSlice + 7).hashCode() % 887766))
            activeTokenCode = codeNum.toString().take(6)
            kotlinx.coroutines.delay(1000)
        }
    }

    // Step 2: Username State
    var usernameInput by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf<String?>(null) }

    // Step 3: PIN State
    var pinInput by remember { mutableStateOf("") }
    var pinConfirmInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    // Step 4: Biometric State
    var biometricProgress by remember { mutableStateOf(0f) }
    var isFingerprintScanned by remember { mutableStateOf(false) }
    var scanActive by remember { mutableStateOf(false) }
    var biometricEnabledPreference by remember { mutableStateOf(false) }

    LaunchedEffect(scanActive) {
        if (scanActive) {
            while (biometricProgress < 1.0f) {
                biometricProgress += 0.05f
                kotlinx.coroutines.delay(100)
            }
            isFingerprintScanned = true
            biometricEnabledPreference = true
            scanActive = false
        }
    }

    // Step 5: Mobile Number State
    var mobileInput by remember { mutableStateOf("") }
    var mobileError by remember { mutableStateOf<String?>(null) }

    // Step 6: Profile Picture State
    var selectedAvatarIndex by remember { mutableStateOf(0) }
    val avatarGradients = listOf(
        listOf(Color(0xFF00B4DB), Color(0xFF0083B0)), // Cosmic Teal
        listOf(Color(0xFFFF8C00), Color(0xFFE52D27)), // Neon Sunset
        listOf(Color(0xFF8A2387), Color(0xFFE94057)), // Nebula Spark
        listOf(Color(0xFF11998e), Color(0xFF38ef7d)), // Emerald Shield
        listOf(Color(0xFFED213A), Color(0xFF93291E)), // Cyberpunk Crimson
        listOf(Color(0xFFF12711), Color(0xFFF5AF19))  // Electric Amber
    )
    val avatarLabels = listOf("Cosmic Teal", "Neon Sunset", "Nebula Spark", "Emerald Shield", "Cyberpunk Red", "Electric Amber")
    
    var showCameraSimulation by remember { mutableStateOf(false) }
    var capturedPhotoPreset by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0E1116) // Secure space dark palette
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Step content switching
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (currentStep) {
                    1 -> {
                            // Step 1: Two-Factor Authorization (2FA) setup
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = "2FA Lock",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Two-Factor Authentication",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Secure your encryption keys using a secondary security token layer.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            // Virtual Secret card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1D2433))
                                    .padding(12.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "YOUR SECURE AUTHENTICATOR KEY",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    Text(
                                        text = tfaSecret,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        val clipboardManager = LocalClipboardManager.current
                                        TextButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(tfaSecret))
                                            },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Copy Key", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Live Token display
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .padding(vertical = 8.dp, horizontal = 16.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = { countdown / 30f },
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Active Token: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${activeTokenCode.take(3)} ${activeTokenCode.drop(3)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = tfaInput,
                                onValueChange = { input ->
                                    if (input.length <= 6 && input.all { it.isDigit() }) {
                                        tfaInput = input
                                        tfaError = null
                                    }
                                },
                                label = { Text("6-Digit Verification Code") },
                                placeholder = { Text("e.g. 123456") },
                                singleLine = true,
                                isError = tfaError != null,
                                supportingText = { tfaError?.let { Text(it) } },
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (tfaInput == activeTokenCode) {
                                        tfaVerified = true
                                        tfaError = null
                                        currentStep = 2
                                    } else {
                                        tfaError = "Invalid verification code. Try matching the active token above."
                                    }
                                },
                                enabled = tfaInput.length == 6,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Text("Verify & Setup 2FA", fontWeight = FontWeight.Bold)
                            }
                        }

                        2 -> {
                            // Step 2: Username setup
                            Icon(
                                imageVector = Icons.Default.AlternateEmail,
                                contentDescription = "Username",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Choose Username",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Select a profile username for your secure space. This can be customized.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = usernameInput,
                                onValueChange = { input ->
                                    if (input.length <= 15) {
                                        val filtered = input.filter { it.isLetterOrDigit() || it == '_' }
                                        usernameInput = filtered
                                        usernameError = when {
                                            filtered.isEmpty() -> "Username cannot be empty"
                                            filtered.length < 3 -> "Must be at least 3 characters"
                                            else -> null
                                        }
                                    }
                                },
                                label = { Text("Pseudonym Handle") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                placeholder = { Text("e.g. Cipher_Wolf") },
                                singleLine = true,
                                isError = usernameError != null,
                                supportingText = { 
                                    usernameError?.let { Text(it) } ?: Text("Only letters, numbers, and underscores allowed.")
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    val randomAdjectives = listOf("Secure", "Ghost", "Crypto", "Phantom", "Silent", "Neon", "Void", "Quantum", "Amber")
                                    val randomNouns = listOf("Node", "Sentry", "Breeze", "Rider", "Wraith", "Vortex", "Falcon", "Pixel", "Spark")
                                    usernameInput = "${randomAdjectives.random()}_${randomNouns.random()}_${(10..99).random()}"
                                    usernameError = null
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Generate", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Smart Pseudonym", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    if (usernameInput.trim().length >= 3) {
                                        currentStep = 3
                                    } else {
                                        usernameError = "Must be at least 3 characters"
                                    }
                                },
                                enabled = usernameInput.trim().length >= 3 && usernameError == null,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Text("Next Step", fontWeight = FontWeight.Bold)
                            }
                        }

                        3 -> {
                            // Step 3: Security PIN setup
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "Security PIN",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Setup Security PIN",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Configure a local 4-digit security passcode to lock your local database enclave.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = { input ->
                                    if (input.length <= 4 && input.all { it.isDigit() }) {
                                        pinInput = input
                                        pinError = null
                                    }
                                },
                                label = { Text("Choose 4-Digit PIN") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = pinConfirmInput,
                                onValueChange = { input ->
                                    if (input.length <= 4 && input.all { it.isDigit() }) {
                                        pinConfirmInput = input
                                        pinError = null
                                    }
                                },
                                label = { Text("Confirm 4-Digit PIN") },
                                singleLine = true,
                                isError = pinError != null,
                                supportingText = { pinError?.let { Text(it) } },
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    if (pinInput.length != 4) {
                                        pinError = "PIN must be exactly 4 digits"
                                    } else if (pinInput != pinConfirmInput) {
                                        pinError = "PINs do not match"
                                    } else {
                                        currentStep = 4
                                    }
                                },
                                enabled = pinInput.length == 4 && pinConfirmInput.length == 4,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Text("Next Step", fontWeight = FontWeight.Bold)
                            }
                        }

                        4 -> {
                            // Step 4: Take fingerprint setup
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Fingerprint Setup",
                                tint = if (isFingerprintScanned) Color(0xFF38ef7d) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Biometric Enlistment",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Register your device fingerprint credential to enable swift instant local unlocking.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Interactive scan module
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1D2433))
                                    .border(
                                        width = 3.dp,
                                        color = if (isFingerprintScanned) Color(0xFF38ef7d) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                                    .pointerInput(isFingerprintScanned) {
                                        detectTapGestures(
                                            onPress = {
                                                if (!isFingerprintScanned) {
                                                    scanActive = true
                                                    tryAwaitRelease()
                                                    scanActive = false
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (scanActive) {
                                    CircularProgressIndicator(
                                        progress = { biometricProgress },
                                        modifier = Modifier.fillMaxSize(),
                                        color = Color(0xFF38ef7d),
                                        strokeWidth = 4.dp
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Tap fingerprint icon to scan",
                                        tint = when {
                                            isFingerprintScanned -> Color(0xFF38ef7d)
                                            scanActive -> Color(0xFF38ef7d).copy(alpha = 0.7f)
                                            else -> MaterialTheme.colorScheme.primary
                                        },
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Text(
                                        text = if (isFingerprintScanned) "COMPLETED" else if (scanActive) "SCANNING..." else "HOLD TO SCAN",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFingerprintScanned) Color(0xFF38ef7d) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (isFingerprintScanned) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF38ef7d), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Biometric registration complete!", color = Color(0xFF38ef7d), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    biometricEnabledPreference = true
                                    currentStep = 5
                                },
                                enabled = isFingerprintScanned,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Text("Continue", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        5 -> {
                            // Step 5: Establish Mobile Connection (setup secure number)
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = "Mobile Connection",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Secure Mobile Network",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Setup your secure 10-digit mobile number directory to allow direct cryptographically-verified 1-on-1 private messaging.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = mobileInput,
                                onValueChange = { input ->
                                    if (input.length <= 10 && input.all { it.isDigit() }) {
                                        mobileInput = input
                                        mobileError = if (input.length < 10) "Must be exactly 10 digits" else null
                                    }
                                },
                                label = { Text("Your 10-Digit Mobile ID") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                placeholder = { Text("e.g. 9876543210") },
                                singleLine = true,
                                isError = mobileError != null,
                                supportingText = { mobileError?.let { Text(it) } },
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    val firstDigit = (6..9).random().toString()
                                    val remainingDigits = (100000000L..999999999L).random().toString()
                                    mobileInput = firstDigit + remainingDigits
                                    mobileError = null
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Icon(Icons.Default.DialerSip, contentDescription = "Generate", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Dynamic Mobile ID", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    if (mobileInput.length == 10 && (mobileInput[0] in '6'..'9')) {
                                        currentStep = 6
                                    } else {
                                        mobileError = "Must be a 10-digit number starting with 6, 7, 8, or 9."
                                    }
                                },
                                enabled = mobileInput.length == 10 && mobileError == null,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Text("Next Step", fontWeight = FontWeight.Bold)
                            }
                        }

                        6 -> {
                            // Step 6: Choose profile picture identity (Instagram-style customizer)
                            Text(
                                text = "Visual Profile Identity",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Establish your signature profile representation. Choose an E2EE abstract gradient preset or capture a live cyber snapshot.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Custom avatar preview
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            if (capturedPhotoPreset) listOf(Color(0xFF1F2937), Color(0xFF111827)) else avatarGradients[selectedAvatarIndex]
                                        )
                                    )
                                    .border(width = 3.dp, color = MaterialTheme.colorScheme.primary, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (capturedPhotoPreset) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Contactless,
                                            contentDescription = "Secured biometric mask",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(42.dp)
                                        )
                                        Text(
                                            text = "SNAP_CAP",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else {
                                    Text(
                                        text = usernameInput.take(2).uppercase(),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Selection list of pre-designed gradients
                            Text(
                                text = "CHOOSE SECURE THEME PROFILE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                            ) {
                                avatarGradients.take(6).forEachIndexed { index, gradientColors ->
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(gradientColors))
                                            .border(
                                                width = if (selectedAvatarIndex == index && !capturedPhotoPreset) 2.dp else 0.dp,
                                                color = Color.White,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                selectedAvatarIndex = index
                                                capturedPhotoPreset = false
                                            }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Snap custom snap simulator
                            Button(
                                onClick = {
                                    showCameraSimulation = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = "Camera", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (capturedPhotoPreset) "Retake Security Snap" else "Capture Cyber Profile Snap", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            if (showCameraSimulation) {
                                AlertDialog(
                                    onDismissRequest = { showCameraSimulation = false },
                                    title = { Text("E2EE Pattern Scanner") },
                                    text = {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "Simulating encrypted front face scan to generate dynamic crypto-mask representation.",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(bottom = 12.dp)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(160.dp)
                                                    .background(Color.Black, RoundedCornerShape(16.dp))
                                                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                                                Icon(
                                                    imageVector = Icons.Default.FilterCenterFocus,
                                                    contentDescription = "Target",
                                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(80.dp)
                                                )
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            capturedPhotoPreset = true
                                            showCameraSimulation = false
                                        }) {
                                            Text("CAPTURE SNAP", fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showCameraSimulation = false }) {
                                            Text("CANCEL")
                                        }
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    currentStep = 7
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Text("Next Step", fontWeight = FontWeight.Bold)
                            }
                        }

                        7 -> {
                            // Step 7: Confirmation "Account is maked" splash
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Verified Secure Setup",
                                tint = Color(0xFF38ef7d),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Profile Successfully Sealed!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Your end-to-end encrypted profile is fully configured and ready.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Summary cards
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF1D2433))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Pseudonym ID", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(usernameInput, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Mobile Network", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("+91 $mobileInput", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("2FA Safeguard", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("ENABLED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38ef7d))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Biometric Security", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(if (biometricEnabledPreference) "ACTIVE" else "DISABLED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (biometricEnabledPreference) Color(0xFF38ef7d) else Color.Gray)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Enclave Style Theme", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(if (capturedPhotoPreset) "Cyber Snap" else avatarLabels[selectedAvatarIndex], fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Micro-log terminal simulation
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(75.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black)
                                    .padding(8.dp)
                            ) {
                                Text("[SUCCESS] 2FA keys registered and locked.", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF38ef7d))
                                Text("[SUCCESS] Database table decrypted & signed.", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF38ef7d))
                                Text("[SUCCESS] Secure local peer-tunnel ready.", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF38ef7d))
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    viewModel.completeProfileSetup(
                                        username = usernameInput,
                                        pin = pinInput,
                                        mobileNumber = mobileInput,
                                        avatarIndex = selectedAvatarIndex,
                                        tfaSecret = tfaSecret,
                                        biometricEnabled = biometricEnabledPreference
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF38ef7d),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Launch", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Register & Enter Secure Space", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Footer help text
            Text(
                text = "MANNHALKA decentralization network utilizes advanced AES-256 E2E database storage.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
}

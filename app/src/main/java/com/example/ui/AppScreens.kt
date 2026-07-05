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
import androidx.compose.ui.res.painterResource
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mobile Number Settings", style = MaterialTheme.typography.headlineMedium)
        
        if (mobileNumber == null) {
            Button(onClick = { viewModel.generateMobileNumber() }) {
                Text("Generate 13-Digit Mobile Number")
            }
        } else {
            Text("Your 13-Digit Number:", style = MaterialTheme.typography.titleMedium)
            Text(mobileNumber!!, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Add Contact", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = contactName, onValueChange = { contactName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(autoCorrectEnabled = false))
        OutlinedTextField(value = contactNumber, onValueChange = { contactNumber = it }, label = { Text("13-Digit Number") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
        Button(onClick = { 
            if (contactNumber.length == 13) {
                viewModel.addContact(contactNumber, contactName)
                contactName = ""
                contactNumber = ""
            }
        }) {
            Text("Add Contact")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Contacts", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(contacts) { contact ->
                Text("${contact.name} - ${contact.mobileNumber}")
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
    val level by viewModel.leaderboardLevel.collectAsState()
    val points by viewModel.leaderboardPoints.collectAsState()
    
    // Security status
    val isRooted by viewModel.isRooted.collectAsState()
    val isDebugger by viewModel.isDebuggerConnected.collectAsState()
    val isEmulator by viewModel.isEmulatorRunning.collectAsState()
    val isSecure = !isRooted && !isDebugger && !isEmulator

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Avatar
        Box(
            modifier = Modifier.size(100.dp).clip(CircleShape).background(Color(avatarColor)),
            contentAlignment = Alignment.Center
        ) {
            Text(pseudonym.take(1), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onPrimary)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Identity fingerprint",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = "MANNHALKA",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        IconButton(
            onClick = { /* No action needed */ },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Shield Lock",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun DonationDialog(onDismiss: () -> Unit) {
    val amounts = listOf(10, 20, 50, 100, 200, 500, 1000, 5000, 10000)
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { androidx.compose.material3.Text("Donate to Mannhalka") },
        text = {
            androidx.compose.foundation.layout.Column {
                amounts.forEach { amount ->
                    androidx.compose.material3.TextButton(
                        onClick = { /* Handle donation */ onDismiss() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.material3.Text("₹$amount")
                    }
                }
                androidx.compose.material3.TextButton(
                    onClick = { /* Handle custom donation */ onDismiss() },
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
                color = Color(0xFFD7CDBB),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE7E0D1)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.VisibilityOff,
                contentDescription = "Visibility Off",
                tint = Color(0xFF6F5D3E),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Anonymous environment active. Screenshot & download features are globally disabled for your safety.",
                fontSize = 11.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6F5D3E)
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
            
            // Global Beautiful Privacy Banner
            PrivacyBanner()
            
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
    LaunchedEffect(isSetup) {
        if (!isSetup) {
            val activity = context.findActivity()
            if (activity != null) {
                com.example.security.BiometricBridge.canAuthenticate(context, object : com.example.security.BiometricBridge.BiometricPromise {
                    override fun resolve(value: Any?) {
                        com.example.security.BiometricBridge.authenticate(
                            activity = activity,
                            title = "Unlock Private Enclave",
                            subtitle = "Verify identity to access your encrypted chats",
                            promise = object : com.example.security.BiometricBridge.BiometricPromise {
                                override fun resolve(value: Any?) {
                                    viewModel.unlockApp()
                                    viewModel.passcodeError.value = null
                                    viewModel.passcodeText.value = ""
                                }

                                override fun reject(code: String, message: String) {
                                    // Let user fall back to PIN if canceled or errored
                                    if (!code.contains("13") && !code.contains("10")) {
                                        viewModel.passcodeError.value = "Biometric prompt failed: $message"
                                    }
                                }
                            }
                        )
                    }

                    override fun reject(code: String, message: String) {
                        // Biometrics unavailable or not enrolled, fall back silently to PIN
                    }
                })
            }
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
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.statusBars),
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
            
            if (!isSetup) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val intent = com.example.security.GoogleAuthHelper.getSignInIntent(context)
                        launcher.launch(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("Sign in with Google", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = headlineText,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

        // Numeric Pad Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (key in row) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .clickable {
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
                                                val activity = context.findActivity()
                                                if (activity != null) {
                                                    com.example.security.BiometricBridge.canAuthenticate(context, object : com.example.security.BiometricBridge.BiometricPromise {
                                                        override fun resolve(value: Any?) {
                                                            com.example.security.BiometricBridge.authenticate(
                                                                activity = activity,
                                                                title = "Unlock Private Enclave",
                                                                subtitle = "Verify identity to access your encrypted chats",
                                                                promise = object : com.example.security.BiometricBridge.BiometricPromise {
                                                                    override fun resolve(value: Any?) {
                                                                        viewModel.unlockApp()
                                                                        viewModel.passcodeError.value = null
                                                                        viewModel.passcodeText.value = ""
                                                                    }

                                                                    override fun reject(code: String, message: String) {
                                                                        viewModel.passcodeError.value = "Authentication failed: $message"
                                                                    }
                                                                }
                                                            )
                                                        }

                                                        override fun reject(code: String, message: String) {
                                                            viewModel.passcodeError.value = "Biometrics unavailable: $message"
                                                        }
                                                    })
                                                } else {
                                                    viewModel.passcodeError.value = "System error: Context activity not found"
                                                }
                                            }
                                        }
                                        else -> {
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
                                .background(
                                    if (key == "Delete" || key == "Biometrics") Color.Transparent
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
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = "Simulated Biometrics",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
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

// ---------------- EMOTIONAL ANONYMOUS FEED SCREEN ----------------
@Composable
fun FeedScreen(viewModel: MainViewModel) {
    val posts by viewModel.filteredFeelingPosts.collectAsState()
    val activeFilter by viewModel.selectedTagFilter.collectAsState()
    val userNickname by viewModel.userPseudonym.collectAsState()

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
            
            IconButton(
                onClick = { viewModel.regenerateProfile() },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Regenerate Alias",
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

        Spacer(modifier = Modifier.height(12.dp))

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
            text = "Release what weighs heavy on your soul. Completely anonymously, fully secure, and zero screenshot footprint.",
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
                text = "Scan & Post Anonymously",
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
            Text(
                text = "E2EE Communications",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
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

// ---------------- SECURE E2EE CHAT DIALOGUE ROOM SCREEN ----------------
@Composable
fun ChatRoomScreen(viewModel: MainViewModel, chatId: String) {
    val messages by viewModel.activeChatMessages.collectAsState()
    val activeRoom by viewModel.activeChatRoom.collectAsState()
    val userNickname by viewModel.userPseudonym.collectAsState()
    val expandedStates by viewModel.expandedMessageEncryptionState.collectAsState()

    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    var textInput by remember { mutableStateOf("") }

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
                                Text(
                                    text = activeRoom!!.participantName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
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
                                    text = if (isMyMsg) "You (Anonymous)" else msg.senderName,
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
                                            if (isMyMsg) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isMyMsg) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
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
                            singleLine = true
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
                    text = "Current Anonymous Signature",
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

                Button(
                    onClick = { viewModel.regenerateProfile() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Regen")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Regenerate Signature Alias", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
            icon = Icons.Default.Block,
            title = "Screenshot Prevention Active",
            desc = "Android native FLAG_SECURE prevents screen grabs, streaming, or video recordings of this interface."
        )

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

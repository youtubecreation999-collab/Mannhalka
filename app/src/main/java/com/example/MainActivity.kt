package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainAppContainer
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel

class MainActivity : FragmentActivity() {
  private lateinit var viewModel: MainViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    // Enable ultimate crash immunity self-healing engine
    com.example.security.SecurityExceptionHandler.initialize(this)
    
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Natively enforce strict security policy: Block all screenshots and screen-recordings
    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    
    setContent {
      val mainViewModel: MainViewModel = viewModel()
      viewModel = mainViewModel
      val selectedThemeId by mainViewModel.selectedThemeId.collectAsState()
      MyApplicationTheme(themeId = selectedThemeId) {
        androidx.compose.runtime.CompositionLocalProvider(
          androidx.compose.ui.platform.LocalTextToolbar provides com.example.ui.EmptyTextToolbar()
        ) {
          MainAppContainer(viewModel = viewModel)
        }
      }
    }
  }

  override fun onStop() {
    super.onStop()
    if (::viewModel.isInitialized) {
        viewModel.clearAllData()
    }
  }

  override fun onPause() {
    super.onPause()
    if (::viewModel.isInitialized) {
        viewModel.setPrivacyMode(true)
    }
  }

  override fun onResume() {
    super.onResume()
    if (::viewModel.isInitialized && !viewModel.isAuthenticated.value && !viewModel.isAuthenticating.value && viewModel.isPasscodeSetup.value) {
        viewModel.isAuthenticating.value = true
        com.example.security.BiometricBridge.authenticate(
            this,
            "Authentication Required",
            "Please authenticate to access the app",
            object : com.example.security.BiometricBridge.BiometricPromise {
                override fun resolve(value: Any?) {
                    viewModel.isAuthenticating.value = false
                    viewModel.unlockApp()
                }
                override fun reject(code: String, message: String) {
                    viewModel.isAuthenticating.value = false
                    // Handle rejection: could finish activity or show error
                }
            }
        )
    }
    if (::viewModel.isInitialized) {
        viewModel.setPrivacyMode(false)
    }
  }
}

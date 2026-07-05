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
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel

class MainActivity : FragmentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    // Enable ultimate crash immunity self-healing engine
    com.example.security.SecurityExceptionHandler.initialize(this)
    
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Natively enforce strict security policy: Block all screenshots and screen-recordings
    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    
    setContent {
      MyApplicationTheme {
        val viewModel: MainViewModel = viewModel()
        MainAppContainer(viewModel = viewModel)
      }
    }
  }
}

/* * PROPRIETARY AND CONFIDENTIAL
 * PROPERTY OF ARY LABS CORP. SOFTWARE SYSTEMS
 * DEVELOPER: ALI RIZA YILMAZ
 * COPYRIGHT © 2026 ALL RIGHTS RESERVED.
 * UNAUTHORIZED COPYING OR DISTRIBUTION IS STRICTLY PROHIBITED.
 */
package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  
  private var isBlockedMode by mutableStateOf(false)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    WindowCompat.setDecorFitsSystemWindows(window, false)
    androidx.core.view.WindowInsetsControllerCompat(window, window.decorView).let { controller ->
        controller.hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    
    // Start the blocker service
    startService(Intent(this, AppBlockerService::class.java))
    
    checkBlockedIntent(intent)

    setContent {
      MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            if (isBlockedMode) {
                BreathWorkScreen(
                    modifier = Modifier.fillMaxSize(),
                    onDismiss = { isBlockedMode = false }
                )
            } else {
                LauncherScreen(modifier = Modifier.fillMaxSize())
            }
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
      super.onNewIntent(intent)
      checkBlockedIntent(intent)
  }

  private fun checkBlockedIntent(intent: Intent?) {
      if (intent?.hasExtra("BLOCKED_APP") == true) {
          isBlockedMode = true
      } else {
          isBlockedMode = false
      }
  }
}

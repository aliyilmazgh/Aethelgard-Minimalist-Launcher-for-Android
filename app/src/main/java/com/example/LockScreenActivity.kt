/* * PROPRIETARY AND CONFIDENTIAL
 * PROPERTY OF ARY LABS CORP. SOFTWARE SYSTEMS
 * DEVELOPER: ALI RIZA YILMAZ
 * COPYRIGHT © 2026 ALL RIGHTS RESERVED.
 * UNAUTHORIZED COPYING OR DISTRIBUTION IS STRICTLY PROHIBITED.
 */
package com.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

class LockScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
            var currentDate by remember { mutableStateOf(SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())) }

            LaunchedEffect(Unit) {
                while (true) {
                    currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                    currentDate = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
                    delay(1000 * 60)
                }
            }
            
            var isFlashlightOn by remember { mutableStateOf(false) }
            val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = remember { 
                try {
                    cameraManager.cameraIdList.firstOrNull { id ->
                        cameraManager.getCameraCharacteristics(id).get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                    }
                } catch(e:Exception) { null }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount < -50) { // Upward swipe
                                finish()
                            }
                        }
                    }
                    .padding(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(top = 48.dp).align(Alignment.TopCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentTime,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.ExtraLight,
                        letterSpacing = (-3).sp,
                        color = Color.White,
                        fontFamily = FontFamily.SansSerif
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentDate.uppercase(Locale.getDefault()),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 4.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontFamily = FontFamily.SansSerif
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            if (cameraId != null) {
                                try {
                                    isFlashlightOn = !isFlashlightOn
                                    cameraManager.setTorchMode(cameraId, isFlashlightOn)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    ) {
                        Icon(
                            com.example.SystemIcons.Flashlight, 
                            contentDescription = "Flashlight", 
                            tint = if (isFlashlightOn) Color.White else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            val intent = Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                            startActivity(intent)
                        }
                    ) {
                        Icon(
                            com.example.SystemIcons.Camera, 
                            contentDescription = "Camera", 
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

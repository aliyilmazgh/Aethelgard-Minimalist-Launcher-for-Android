/* * PROPRIETARY AND CONFIDENTIAL
 * PROPERTY OF ARY LABS CORP. SOFTWARE SYSTEMS
 * DEVELOPER: ALI RIZA YILMAZ
 * COPYRIGHT © 2026 ALL RIGHTS RESERVED.
 * UNAUTHORIZED COPYING OR DISTRIBUTION IS STRICTLY PROHIBITED.
 */
package com.example

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AppItem(val label: String, val packageName: String, val intent: Intent)

enum class LauncherPage { Home, AppList }

@Composable
fun LauncherScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var installedApps by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    var vaultUnlocked by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        installedApps = loadApps(context)
    }

    val visibleApps = remember(installedApps, vaultUnlocked) {
        if (vaultUnlocked) {
            installedApps
        } else {
            installedApps.filter { !isBlacklisted(it.packageName) }
        }
    }

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> HomeScreen(
                    onSearchClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    onSettingsClick = {
                        showSettings = true
                    },
                    installedApps = visibleApps,
                    context = context
                )
                1 -> AppListScreen(
                    apps = visibleApps,
                    context = context
                )
            }
        }
        
        AnimatedVisibility(
            visible = showSettings,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            SettingsOverlay(onClose = { showSettings = false })
        }
        
        // Vault unlock zone at the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .align(Alignment.TopCenter)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            vaultUnlocked = !vaultUnlocked
                        }
                    )
                },
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .width(32.dp)
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            )
        }
    }
}

@Composable
fun HomeScreen(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    installedApps: List<AppItem>,
    context: Context
) {
    // We get 3 specific apps for the home screen
    val homeApps = remember(installedApps) {
        val targets = listOf("phone", "messages", "chrome", "camera")
        val found = installedApps.filter { app ->
            targets.any { target -> app.label.lowercase().contains(target) }
        }
        if (found.isNotEmpty()) found.take(3) else installedApps.take(3)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
        var currentDate by remember { mutableStateOf(SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())) }

        LaunchedEffect(Unit) {
            while (true) {
                currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                currentDate = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
                kotlinx.coroutines.delay(1000 * 60)
            }
        }

        // Clock at Absolute Top Center
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentTime,
                fontSize = 72.sp,
                fontWeight = FontWeight.ExtraLight,
                letterSpacing = (-3).sp,
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.clickable {
                    try {
                        val intent = Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS)
                        context.startActivity(intent)
                    } catch(e:Exception){}
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentDate.uppercase(Locale.getDefault()),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 4.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.clickable {
                    try {
                        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR)
                        context.startActivity(intent)
                    } catch(e:Exception){}
                }
            )
        }

        // 3 Pinned Apps in Center
        Column(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            homeApps.forEach { app ->
                Text(
                    text = app.label,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1).sp,
                    modifier = Modifier.clickable {
                        try {
                            context.startActivity(app.intent)
                        } catch (e: Exception) { }
                    }
                )
            }
        }

        // Icons at Absolute Bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    try {
                        val intent = Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                        context.startActivity(intent)
                    } catch(e:Exception){}
                }
            ) {
                Icon(
                    imageVector = com.example.SystemIcons.Camera,
                    contentDescription = "Camera",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            IconButton(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL)
                        context.startActivity(intent)
                    } catch(e:Exception){}
                }
            ) {
                Icon(
                    imageVector = com.example.SystemIcons.Phone,
                    contentDescription = "Phone",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        // Stealth Hooks for Search and Settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(onClick = onSettingsClick)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(onClick = onSearchClick)
            )
        }
    }
}

@Composable
fun AppListScreen(
    apps: List<AppItem>,
    context: Context
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredApps = remember(searchQuery, apps) {
        if (searchQuery.isEmpty()) {
            apps
        } else {
            apps.filter { it.label.contains(searchQuery, ignoreCase = true) }
        }
    }

    val lazyListState = rememberLazyListState()
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toList()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 32.dp)
        ) {
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 32.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1).sp
                ),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp, end = 24.dp, top = 24.dp),
                decorationBox = { innerTextField ->
                    Box {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search...",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 32.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Light,
                                letterSpacing = (-1).sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp, top = 16.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    Text(
                        text = app.label,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    context.startActivity(app.intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            .padding(vertical = 14.dp)
                    )
                }
            }
        }
        
        // A-Z Sidebar
        val coroutineScope = rememberCoroutineScope()
        Column(
            modifier = Modifier
                .padding(end = 6.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            alphabet.forEach { letter ->
                Text(
                    text = letter.toString(),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .clickable {
                            val index = filteredApps.indexOfFirst { it.label.uppercase(Locale.getDefault()).startsWith(letter.toString()) }
                            if (index >= 0) {
                                coroutineScope.launch {
                                    lazyListState.scrollToItem(index)
                                }
                            }
                        }
                        .padding(vertical = 2.dp, horizontal = 12.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsOverlay(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.systemBars)
            .clickable(enabled = false, onClick = {}) // block taps from passing through
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp, top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SETTINGS",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "CLOSE",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    modifier = Modifier.clickable(onClick = onClose).padding(8.dp)
                )
            }

            val settingsItems = listOf(
                "Home Screen Config",
                "Display",
                "Gestures",
                "Advanced App Blocker"
            )

            settingsItems.forEach { item ->
                Text(
                    text = item,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.fillMaxWidth().clickable {}.padding(vertical = 16.dp)
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Ary Labs System Status
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "ARY LABS SYSTEM STATUS",
                    color = Color(0xFF00FF00),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = "All Premium Features Unlocked.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = "App Blocker active. Vault accessible.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AppListItem(app: AppItem, onClick: () -> Unit) {
    Text(
        text = app.label,
        color = Color.White,
        fontSize = 24.sp,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        letterSpacing = (-0.5).sp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp)
    )
}

@Composable
fun BreathWorkScreen(modifier: Modifier = Modifier, onDismiss: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "breath")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onDismiss() }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .scale(scale)
                .fillMaxSize(0.3f)
                .drawBehind {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f),
                        radius = size.width / 2f
                    )
                }
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Breathe.",
                color = Color.White,
                fontSize = 24.sp,
                fontFamily = FontFamily.Serif,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Long press to return",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

private suspend fun loadApps(context: Context): List<AppItem> = withContext(Dispatchers.IO) {
    val packageManager = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    
    val activities: List<ResolveInfo> = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        packageManager.queryIntentActivities(
            intent,
            PackageManager.ResolveInfoFlags.of(0L)
        )
    } else {
        packageManager.queryIntentActivities(intent, 0)
    }

    activities.mapNotNull { resolveInfo ->
        val label = resolveInfo.loadLabel(packageManager).toString()
        val packageName = resolveInfo.activityInfo.packageName
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        
        if (packageName == context.packageName || launchIntent == null) {
            null
        } else {
            AppItem(label = label, packageName = packageName, intent = launchIntent)
        }
    }.sortedBy { it.label.lowercase() }
}

private fun isBlacklisted(packageName: String): Boolean {
    val blacklisted = setOf(
        "com.instagram.android",
        "com.zhiliaoapp.musically",
        "com.facebook.katana",
        "com.twitter.android",
        "com.reddit.frontpage",
        "com.google.android.apps.youtube",
        "com.snapchat.android"
    )
    return blacklisted.contains(packageName)
}

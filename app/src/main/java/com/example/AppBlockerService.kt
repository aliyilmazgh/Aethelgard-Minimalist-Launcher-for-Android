/* * PROPRIETARY AND CONFIDENTIAL
 * PROPERTY OF ARY LABS CORP. SOFTWARE SYSTEMS
 * DEVELOPER: ALI RIZA YILMAZ
 * COPYRIGHT © 2026 ALL RIGHTS RESERVED.
 * UNAUTHORIZED COPYING OR DISTRIBUTION IS STRICTLY PROHIBITED.
 */
package com.example

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AppBlockerService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var screenReceiver: android.content.BroadcastReceiver? = null
    
    // Addictive apps list based on package names
    private val blacklistedPackages = setOf(
        "com.instagram.android",
        "com.zhiliaoapp.musically", // TikTok
        "com.facebook.katana",
        "com.twitter.android",
        "com.reddit.frontpage"
    )

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val filter = android.content.IntentFilter(Intent.ACTION_SCREEN_OFF)
        screenReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                    val lockIntent = Intent(context, LockScreenActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    context?.startActivity(lockIntent)
                }
            }
        }
        registerReceiver(screenReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        serviceScope.launch {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            var lastCheckedTime = System.currentTimeMillis()

            while (isActive) {
                val currentTime = System.currentTimeMillis()
                val events = usageStatsManager.queryEvents(lastCheckedTime, currentTime)
                val event = UsageEvents.Event()

                var latestForegroundPackage: String? = null

                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        latestForegroundPackage = event.packageName
                    }
                }

                if (latestForegroundPackage != null && blacklistedPackages.contains(latestForegroundPackage)) {
                    Log.d("AppBlockerService", "Blocked app launched: $latestForegroundPackage")
                    // Intercept and redirect to launcher
                    val redirectIntent = Intent(this@AppBlockerService, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("BLOCKED_APP", latestForegroundPackage)
                    }
                    startActivity(redirectIntent)
                }

                lastCheckedTime = currentTime
                delay(1000) // Poll every second
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        screenReceiver?.let { unregisterReceiver(it) }
    }
}

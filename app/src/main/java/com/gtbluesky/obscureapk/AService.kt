package com.gtbluesky.obscureapk

import android.app.Service
import android.content.Intent
import android.os.IBinder

class AService : Service() {

    fun test() {

    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
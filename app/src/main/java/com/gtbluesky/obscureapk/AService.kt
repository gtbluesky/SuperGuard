package com.gtbluesky.obscureapk

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlin.concurrent.thread

class AService : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.d("AService", "onCreate")
//        thread {
//            while (true) {
//                Log.d("AService", "Time=${System.currentTimeMillis()}")
//                Thread.sleep(1000)
//            }
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AService", "onDestroy")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
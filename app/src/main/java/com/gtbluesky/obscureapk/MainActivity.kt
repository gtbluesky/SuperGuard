package com.gtbluesky.obscureapk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.gtbluesky.obscureapk.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.titleTv.text = """
            当前Activity类名为：${MainActivity::class.java.name}
            当前View类名为：${binding.titleTv::class.java.name}
        """.trimIndent()
    }
}
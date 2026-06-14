package com.yourname.thrive

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("thrive_prefs", MODE_PRIVATE)
        if (prefs.getInt("user_id", -1) != -1) {
            startActivity(Intent(this, DashboardActivity::class.java))
        } else {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        finish()
    }
}
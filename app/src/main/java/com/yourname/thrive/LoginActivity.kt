package com.yourname.thrive

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = DatabaseHelper(this)
        prefs = getSharedPreferences("thrive_prefs", MODE_PRIVATE)

        // If already logged in, go straight to dashboard
        if (prefs.getInt("user_id", -1) != -1) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin   = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cursor = db.readableDatabase.query(
                DatabaseHelper.TABLE_USERS, null,
                "${DatabaseHelper.COL_USERNAME}=? AND ${DatabaseHelper.COL_PASSWORD}=?",
                arrayOf(username, password), null, null, null
            )

            if (cursor.moveToFirst()) {
                val userId   = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID))
                val fullName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME))
                cursor.close()

                // Save session
                prefs.edit()
                    .putInt("user_id", userId)
                    .putString("full_name", fullName)
                    .apply()

                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                cursor.close()
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
}
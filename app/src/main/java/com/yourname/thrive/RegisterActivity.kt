package com.yourname.thrive

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.content.ContentValues

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = DatabaseHelper(this)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnCreate = findViewById<Button>(R.id.btnCreateAccount)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        btnCreate.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            when {
                fullName.isEmpty() -> Toast.makeText(this, "Enter your full name", Toast.LENGTH_SHORT).show()
                username.isEmpty() -> Toast.makeText(this, "Enter a username", Toast.LENGTH_SHORT).show()
                password.length < 8 -> Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                password != confirmPassword -> Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                else -> {
                    val values = ContentValues().apply {
                        put(DatabaseHelper.COL_FULL_NAME, fullName)
                        put(DatabaseHelper.COL_USERNAME, username)
                        put(DatabaseHelper.COL_PASSWORD, password)
                    }
                    val result = db.writableDatabase.insert(DatabaseHelper.TABLE_USERS, null, values)
                    if (result == -1L) {
                        Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                }
            }
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
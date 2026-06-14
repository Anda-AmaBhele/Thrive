package com.yourname.thrive

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences("thrive_prefs", MODE_PRIVATE)

        val btnBack       = findViewById<ImageButton>(R.id.btnBack)
        val btnLogout     = findViewById<Button>(R.id.btnLogout)
        val btnSetPin     = findViewById<Button>(R.id.btnSetPin)
        val spinnerCurrency = findViewById<Spinner>(R.id.spinnerCurrency)
        val tvUsername    = findViewById<TextView>(R.id.tvUsername)
        val tvFullName    = findViewById<TextView>(R.id.tvFullName)

        // Show user info
        tvFullName.text = prefs.getString("full_name", "User")
        tvUsername.text = "@" + prefs.getString("username", "")

        // Currency spinner
        val currencies = listOf("ZAR - South African Rand", "USD - US Dollar",
            "EUR - Euro", "GBP - British Pound", "NGN - Nigerian Naira",
            "KES - Kenyan Shilling", "GHS - Ghanaian Cedi")
        val currentCurrency = prefs.getString("currency", "ZAR - South African Rand")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = adapter
        spinnerCurrency.setSelection(currencies.indexOf(currentCurrency).coerceAtLeast(0))
        spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>, v: android.view.View?, pos: Int, id: Long) {
                prefs.edit().putString("currency", currencies[pos]).apply()
                Toast.makeText(this@SettingsActivity, "Currency updated!", Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(p: AdapterView<*>) {}
        }

        // Set PIN
        btnSetPin.setOnClickListener {
            val input = EditText(this)
            input.hint = "Enter 4-digit PIN"
            input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            AlertDialog.Builder(this)
                .setTitle("Set Passcode Lock")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    val pin = input.text.toString()
                    if (pin.length == 4) {
                        prefs.edit().putString("pin", pin).putBoolean("pin_enabled", true).apply()
                        Toast.makeText(this, "✅ PIN set successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "PIN must be 4 digits", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Logout
        btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out") { _, _ ->
                    prefs.edit().putInt("user_id", -1).remove("full_name").remove("username").apply()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnBack.setOnClickListener { finish() }
    }
}
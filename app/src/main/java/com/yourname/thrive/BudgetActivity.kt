package com.yourname.thrive

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class BudgetActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        db = DatabaseHelper(this)
        prefs = getSharedPreferences("thrive_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        val etBudget = findViewById<EditText>(R.id.etMonthlyBudget)
        val btnSave  = findViewById<Button>(R.id.btnSaveBudget)
        val listView = findViewById<ListView>(R.id.listBudgetCategories)
        val btnBack  = findViewById<ImageButton>(R.id.btnBack)
        val tvCurrentBudget = findViewById<TextView>(R.id.tvCurrentBudget)

        val month = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val currentBudget = prefs.getFloat("monthly_budget", 4000f)
        tvCurrentBudget.text = "Current Budget: R%.2f".format(currentBudget)

        loadBudgetList(userId, month, listView)

        btnSave.setOnClickListener {
            val amount = etBudget.text.toString().trim()
            if (amount.isEmpty() || amount.toDoubleOrNull() == null || amount.toDouble() <= 0) {
                etBudget.error = "Enter a valid amount"
                return@setOnClickListener
            }
            prefs.edit().putFloat("monthly_budget", amount.toFloat()).apply()
            tvCurrentBudget.text = "Current Budget: R%.2f".format(amount.toFloat())
            Toast.makeText(this, "✅ Budget updated!", Toast.LENGTH_SHORT).show()
            etBudget.text.clear()
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun loadBudgetList(userId: Int, month: String, listView: ListView) {
        val cursor = db.readableDatabase.rawQuery("""
            SELECT c.${DatabaseHelper.COL_CAT_NAME},
                   IFNULL(SUM(e.${DatabaseHelper.COL_EXP_AMOUNT}), 0) AS spent
            FROM ${DatabaseHelper.TABLE_CATEGORIES} c
            LEFT JOIN ${DatabaseHelper.TABLE_EXPENSES} e
              ON c.${DatabaseHelper.COL_CAT_ID} = e.${DatabaseHelper.COL_EXP_CATEGORY_ID}
              AND e.${DatabaseHelper.COL_EXP_DATE} LIKE '$month%'
            WHERE c.${DatabaseHelper.COL_CAT_USER_ID} = $userId
            GROUP BY c.${DatabaseHelper.COL_CAT_ID}
            ORDER BY spent DESC
        """, null)

        val items = mutableListOf<String>()
        while (cursor.moveToNext()) {
            val name  = cursor.getString(0)
            val spent = cursor.getDouble(1)
            val icon  = when {
                spent == 0.0 -> "✅"
                spent < 500  -> "🟡"
                else         -> "🔴"
            }
            items.add("$icon  $name  —  R%.2f this month".format(spent))
        }
        cursor.close()
        if (items.isEmpty()) items.add("No categories found.")
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
    }
}
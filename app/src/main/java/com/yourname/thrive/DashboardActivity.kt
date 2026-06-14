package com.yourname.thrive

import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        prefs = getSharedPreferences("thrive_prefs", MODE_PRIVATE)
        db = DatabaseHelper(this)

        val userId = prefs.getInt("user_id", -1)
        val fullName = prefs.getString("full_name", "there")

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }

        findViewById<TextView>(R.id.tvGreeting).text = "$greeting, $fullName! 👋"

        ensureDefaultCategories(userId)

        val month = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val monthlyBudget = prefs.getFloat("monthly_budget", 4000f).toDouble()

        val cursor = db.readableDatabase.rawQuery(
            "SELECT SUM(${DatabaseHelper.COL_EXP_AMOUNT}) FROM ${DatabaseHelper.TABLE_EXPENSES} " +
                    "WHERE ${DatabaseHelper.COL_EXP_USER_ID}=? AND ${DatabaseHelper.COL_EXP_DATE} LIKE '$month%'",
            arrayOf(userId.toString())
        )
        var totalSpent = 0.0
        if (cursor.moveToFirst()) totalSpent = cursor.getDouble(0)
        cursor.close()

        findViewById<TextView>(R.id.tvTotalSpent).text = "R%.2f".format(totalSpent)
        findViewById<TextView>(R.id.tvBudgetTotal).text = "/ R%.2f".format(monthlyBudget)

        val progress = if (monthlyBudget > 0) ((totalSpent / monthlyBudget) * 100).toInt() else 0
        findViewById<ProgressBar>(R.id.progressBudget).progress = progress.coerceAtMost(100)

        findViewById<ProgressBar>(R.id.progressBudget).setOnClickListener {
            startActivity(Intent(this, SpendingInsightsActivity::class.java))
        }

        loadCategoryAmount(userId, month, "Food", R.id.tvFoodAmount)
        loadCategoryAmount(userId, month, "Transport", R.id.tvTransportAmount)
        loadCategoryAmount(userId, month, "Entertainment", R.id.tvEntertainmentAmount)
        loadCategoryAmount(userId, month, "Data", R.id.tvDataAmount)
        loadCategoryAmount(userId, month, "Groceries", R.id.tvGroceriesAmount)
        loadCategoryAmount(userId, month, "Utilities", R.id.tvUtilitiesAmount)

        findViewById<FloatingActionButton>(R.id.fabAddExpense).setOnClickListener {
            Log.d("Dashboard", "FAB clicked")
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_expenses -> {
                    startActivity(Intent(this, ExpenseListActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_budget -> {
                    startActivity(Intent(this, BudgetActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_savings -> {
                    startActivity(Intent(this, SavingsActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_badges -> {
                    startActivity(Intent(this, BadgesActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    private fun loadCategoryAmount(userId: Int, month: String, categoryName: String, viewId: Int) {
        val cursor = db.readableDatabase.rawQuery("""
            SELECT IFNULL(SUM(e.${DatabaseHelper.COL_EXP_AMOUNT}), 0)
            FROM ${DatabaseHelper.TABLE_EXPENSES} e
            JOIN ${DatabaseHelper.TABLE_CATEGORIES} c
            ON e.${DatabaseHelper.COL_EXP_CATEGORY_ID} = c.${DatabaseHelper.COL_CAT_ID}
            WHERE e.${DatabaseHelper.COL_EXP_USER_ID} = $userId
            AND c.${DatabaseHelper.COL_CAT_NAME} = '$categoryName'
            AND e.${DatabaseHelper.COL_EXP_DATE} LIKE '$month%'
        """, null)
        var amount = 0.0
        if (cursor.moveToFirst()) amount = cursor.getDouble(0)
        cursor.close()
        findViewById<TextView>(viewId).text = "R%.2f".format(amount)
    }

    private fun ensureDefaultCategories(userId: Int) {
        val cursor = db.readableDatabase.query(
            DatabaseHelper.TABLE_CATEGORIES, null,
            "${DatabaseHelper.COL_CAT_USER_ID}=?",
            arrayOf(userId.toString()), null, null, null
        )
        val count = cursor.count
        cursor.close()
        if (count == 0) {
            val defaults = listOf(
                Pair("Food", "#FF6D00"),
                Pair("Transport", "#212121"),
                Pair("Entertainment", "#C62828"),
                Pair("Data", "#1565C0"),
                Pair("Groceries", "#2E7D32"),
                Pair("Utilities", "#00897B"),
                Pair("Health", "#AD1457"),
                Pair("Education", "#6A1B9A"),
                Pair("Clothing", "#4527A0"),
                Pair("Savings", "#00838F"),
                Pair("Eating Out", "#E65100"),
                Pair("Other", "#546E7A")
            )
            defaults.forEach { (name, color) ->
                val cv = ContentValues().apply {
                    put(DatabaseHelper.COL_CAT_NAME, name)
                    put(DatabaseHelper.COL_CAT_COLOR, color)
                    put(DatabaseHelper.COL_CAT_USER_ID, userId)
                }
                db.writableDatabase.insert(DatabaseHelper.TABLE_CATEGORIES, null, cv)
            }
        }
    }
}
package com.yourname.thrive

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class BadgesActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badges)

        db = DatabaseHelper(this)
        prefs = getSharedPreferences("thrive_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        val listView = findViewById<ListView>(R.id.listBadges)
        val btnBack  = findViewById<ImageButton>(R.id.btnBack)
        val tvPoints = findViewById<TextView>(R.id.tvPoints)

        // --- Count total expenses ---
        val cursorExp = db.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_EXPENSES} WHERE ${DatabaseHelper.COL_EXP_USER_ID}=?",
            arrayOf(userId.toString())
        )
        var expenseCount = 0
        if (cursorExp.moveToFirst()) expenseCount = cursorExp.getInt(0)
        cursorExp.close()

        // --- Count savings goals ---
        val cursorSav = db.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_SAVINGS} WHERE ${DatabaseHelper.COL_SAV_USER_ID}=?",
            arrayOf(userId.toString())
        )
        var savingsCount = 0
        if (cursorSav.moveToFirst()) savingsCount = cursorSav.getInt(0)
        cursorSav.close()

        // --- Calculate streak ---
        val streak = calculateStreak(userId)

        // --- Check if under budget this month ---
        val underBudget = isUnderBudgetThisMonth(userId)

        // --- Calculate points ---
        val points = (expenseCount * 10) + (savingsCount * 25) + (streak * 15) + (if (underBudget) 50 else 0)

        val level = when {
            points >= 500 -> "🏆 Champion"
            points >= 200 -> "🥇 Thriver"
            points >= 100 -> "🥈 Planner"
            points >= 10  -> "🥉 Saver"
            else          -> "🌱 Beginner"
        }

        tvPoints.text = "Level: $level  |  Points: $points  |  🔥 Streak: ${streak}d"

        // --- Build badge list ---
        val badges = mutableListOf<String>()

        // Expense badges
        if (expenseCount >= 1)  badges.add("✅ First Step — Logged your first expense")
        else                    badges.add("🔒 First Step — Log your first expense")

        if (expenseCount >= 5)  badges.add("✅ Getting Started — Logged 5 expenses")
        else                    badges.add("🔒 Getting Started — Log 5 expenses")

        if (expenseCount >= 20) badges.add("✅ Expense Tracker — Logged 20 expenses")
        else                    badges.add("🔒 Expense Tracker — Log 20 expenses")

        if (expenseCount >= 50) badges.add("✅ Power Tracker — Logged 50 expenses")
        else                    badges.add("🔒 Power Tracker — Log 50 expenses")

        // Savings badges
        if (savingsCount >= 1)  badges.add("✅ Dream Big — Created your first savings goal")
        else                    badges.add("🔒 Dream Big — Create a savings goal")

        if (savingsCount >= 3)  badges.add("✅ Goal Setter — Created 3 savings goals")
        else                    badges.add("🔒 Goal Setter — Create 3 savings goals")

        // Streak badges
        if (streak >= 3)        badges.add("✅ 3-Day Streak — Logged expenses 3 days in a row")
        else                    badges.add("🔒 3-Day Streak — Log expenses 3 days in a row")

        if (streak >= 7)        badges.add("✅ 7-Day Streak — Logged expenses 7 days in a row!")
        else                    badges.add("🔒 7-Day Streak — Log expenses 7 days in a row")

        // Budget badges
        if (underBudget)        badges.add("✅ Budget Champion — Stayed under budget this month!")
        else                    badges.add("🔒 Budget Champion — Stay under budget this month")

        // Points badges
        if (points >= 100)      badges.add("✅ Planner — Reached 100 points")
        else                    badges.add("🔒 Planner — Reach 100 points")

        if (points >= 200)      badges.add("✅ Thriver — Reached 200 points")
        else                    badges.add("🔒 Thriver — Reach 200 points")

        if (points >= 500)      badges.add("✅ Champion — Reached 500 points")
        else                    badges.add("🔒 Champion — Reach 500 points")

        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, badges)
        btnBack.setOnClickListener { finish() }
    }

    private fun calculateStreak(userId: Int): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        var streak = 0
        var checking = true

        while (checking) {
            val dateStr = sdf.format(cal.time)
            val cursor = db.readableDatabase.rawQuery(
                """SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_EXPENSES}
                   WHERE ${DatabaseHelper.COL_EXP_USER_ID}=?
                   AND ${DatabaseHelper.COL_EXP_DATE}=?""",
                arrayOf(userId.toString(), dateStr)
            )
            var count = 0
            if (cursor.moveToFirst()) count = cursor.getInt(0)
            cursor.close()

            if (count > 0) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                checking = false
            }

            if (streak > 365) checking = false
        }
        return streak
    }

    private fun isUnderBudgetThisMonth(userId: Int): Boolean {
        val month = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val monthlyBudget = prefs.getFloat("monthly_budget", 4000f)

        val cursor = db.readableDatabase.rawQuery(
            """SELECT IFNULL(SUM(${DatabaseHelper.COL_EXP_AMOUNT}), 0)
               FROM ${DatabaseHelper.TABLE_EXPENSES}
               WHERE ${DatabaseHelper.COL_EXP_USER_ID}=?
               AND ${DatabaseHelper.COL_EXP_DATE} LIKE '$month%'""",
            arrayOf(userId.toString())
        )
        var total = 0f
        if (cursor.moveToFirst()) total = cursor.getFloat(0)
        cursor.close()

        return total < monthlyBudget && total > 0
    }
}
package com.yourname.thrive

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import java.text.SimpleDateFormat
import java.util.*

class SpendingInsightsActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spending_insights)

        db    = DatabaseHelper(this)
        prefs = getSharedPreferences("thrive_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        val pieChart     = findViewById<PieChart>(R.id.pieChart)
        val tvGoalStatus = findViewById<TextView>(R.id.tvGoalStatus)
        val progressGoal = findViewById<ProgressBar>(R.id.progressGoal)
        val tvGoalLabel  = findViewById<TextView>(R.id.tvGoalLabel)
        val tvTip        = findViewById<TextView>(R.id.tvTip)
        val btnBack      = findViewById<ImageButton>(R.id.btnBack)

        val month         = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val monthlyBudget = prefs.getFloat("monthly_budget", 4000f)

        val categoryColors = mapOf(
            "Food"          to Color.parseColor("#FF6D00"),
            "Transport"     to Color.parseColor("#37474F"),
            "Entertainment" to Color.parseColor("#C62828"),
            "Data"          to Color.parseColor("#1565C0"),
            "Groceries"     to Color.parseColor("#2E7D32"),
            "Utilities"     to Color.parseColor("#00897B"),
            "Health"        to Color.parseColor("#AD1457"),
            "Education"     to Color.parseColor("#6A1B9A"),
            "Clothing"      to Color.parseColor("#4527A0"),
            "Eating Out"    to Color.parseColor("#E65100"),
            "Other"         to Color.parseColor("#546E7A")
        )

        // Load spending by category
        val cursor = db.readableDatabase.rawQuery("""
            SELECT c.${DatabaseHelper.COL_CAT_NAME},
                   IFNULL(SUM(e.${DatabaseHelper.COL_EXP_AMOUNT}), 0) AS spent
            FROM ${DatabaseHelper.TABLE_CATEGORIES} c
            LEFT JOIN ${DatabaseHelper.TABLE_EXPENSES} e
              ON c.${DatabaseHelper.COL_CAT_ID} = e.${DatabaseHelper.COL_EXP_CATEGORY_ID}
              AND e.${DatabaseHelper.COL_EXP_DATE} LIKE '$month%'
            WHERE c.${DatabaseHelper.COL_CAT_USER_ID} = $userId
            GROUP BY c.${DatabaseHelper.COL_CAT_ID}
            HAVING spent > 0
            ORDER BY spent DESC
        """, null)

        val pieEntries = mutableListOf<PieEntry>()
        val pieColors  = mutableListOf<Int>()
        var totalSpent = 0f
        var topCat     = ""
        var topAmount  = 0f

        while (cursor.moveToNext()) {
            val name  = cursor.getString(0)
            val spent = cursor.getFloat(1)
            totalSpent += spent
            pieEntries.add(PieEntry(spent, name))
            pieColors.add(categoryColors[name] ?: Color.GRAY)
            if (spent > topAmount) { topAmount = spent; topCat = name }
        }
        cursor.close()

        // Build pie chart
        if (pieEntries.isNotEmpty()) {
            val dataSet = PieDataSet(pieEntries, "").apply {
                colors        = pieColors
                sliceSpace    = 2f
                valueTextSize = 11f
                valueTextColor = Color.WHITE
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float) = "${value.toInt()}%"
                }
            }
            pieChart.apply {
                data              = PieData(dataSet)
                description.isEnabled = false
                isDrawHoleEnabled = true
                holeRadius        = 52f
                setHoleColor(Color.WHITE)
                setCenterText("R${"%.0f".format(totalSpent)}\nspent")
                setCenterTextSize(14f)
                setCenterTextColor(Color.parseColor("#6C3FC5"))
                legend.isEnabled  = true
                animateY(1000)
                invalidate()
            }
        } else {
            pieChart.setNoDataText("No expenses this month yet")
        }

        // Budget goal status
        val pct = if (monthlyBudget > 0)
            ((totalSpent / monthlyBudget) * 100).toInt().coerceIn(0, 100) else 0
        progressGoal.progress = pct

        val (statusText, tip) = when {
            totalSpent > monthlyBudget -> Pair(
                "🔴 Over Budget — R${"%.2f".format(totalSpent)} / R${"%.2f".format(monthlyBudget)}",
                "You've exceeded your budget! Consider reducing spending on $topCat next month."
            )
            totalSpent > monthlyBudget * 0.85f -> Pair(
                "🟡 Approaching Limit — R${"%.2f".format(totalSpent)} / R${"%.2f".format(monthlyBudget)}",
                "You're close to your limit! Try cutting back on $topCat for the rest of the month."
            )
            totalSpent > 0 -> Pair(
                "✅ On Track — R${"%.2f".format(totalSpent)} / R${"%.2f".format(monthlyBudget)}",
                "Great work staying within budget! $topCat is your biggest category at R${"%.2f".format(topAmount)}."
            )
            else -> Pair(
                "🌱 No expenses logged yet",
                "Start logging your daily expenses to get personalised insights and tips!"
            )
        }

        tvGoalStatus.text = statusText
        tvGoalLabel.text  = "Budget used: $pct%  |  Budget: R${"%.0f".format(monthlyBudget)}"
        tvTip.text        = tip

        btnBack.setOnClickListener { finish() }
    }
}
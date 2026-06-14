package com.yourname.thrive

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class ReportsActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var prefs: SharedPreferences
    private lateinit var barChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        db = DatabaseHelper(this)
        prefs = getSharedPreferences("thrive_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        barChart = findViewById(R.id.barChart)
        val spinnerPeriod = findViewById<Spinner>(R.id.spinnerPeriod)
        val tvInsight     = findViewById<TextView>(R.id.tvInsight)
        val btnBack       = findViewById<ImageButton>(R.id.btnBack)

        val periods = listOf("This Month", "Last Month", "Last 3 Months")
        spinnerPeriod.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, periods
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinnerPeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>, v: android.view.View?, pos: Int, id: Long) {
                loadChart(userId, periods[pos], tvInsight)
            }
            override fun onNothingSelected(p: AdapterView<*>) {}
        }

        loadChart(userId, "This Month", tvInsight)
        btnBack.setOnClickListener { finish() }
    }

    private fun loadChart(userId: Int, period: String, tvInsight: TextView) {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val cal = Calendar.getInstance()

        val months = when (period) {
            "Last Month" -> {
                cal.add(Calendar.MONTH, -1)
                listOf(sdf.format(cal.time))
            }
            "Last 3 Months" -> {
                val list = mutableListOf<String>()
                repeat(3) {
                    list.add(0, sdf.format(cal.time))
                    cal.add(Calendar.MONTH, -1)
                }
                list
            }
            else -> listOf(sdf.format(cal.time))
        }

        val categories = listOf(
            Pair("Food",          Color.parseColor("#FF6D00")),
            Pair("Transport",     Color.parseColor("#37474F")),
            Pair("Entertainment", Color.parseColor("#C62828")),
            Pair("Data",          Color.parseColor("#1565C0")),
            Pair("Groceries",     Color.parseColor("#2E7D32")),
            Pair("Utilities",     Color.parseColor("#00897B")),
            Pair("Health",        Color.parseColor("#AD1457")),
            Pair("Education",     Color.parseColor("#6A1B9A")),
            Pair("Clothing",      Color.parseColor("#4527A0")),
            Pair("Eating Out",    Color.parseColor("#E65100")),
            Pair("Other",         Color.parseColor("#546E7A"))
        )

        val entries    = mutableListOf<BarEntry>()
        val colors     = mutableListOf<Int>()
        val labels     = mutableListOf<String>()
        var maxSpent   = 0f
        var topCat     = ""
        var totalSpent = 0f

        categories.forEach { (catName, barColor) ->
            val monthFilter = months.joinToString(" OR ") {
                "e.${DatabaseHelper.COL_EXP_DATE} LIKE '$it%'"
            }
            val cursor = db.readableDatabase.rawQuery("""
                SELECT IFNULL(SUM(e.${DatabaseHelper.COL_EXP_AMOUNT}), 0)
                FROM ${DatabaseHelper.TABLE_EXPENSES} e
                JOIN ${DatabaseHelper.TABLE_CATEGORIES} c
                  ON e.${DatabaseHelper.COL_EXP_CATEGORY_ID} = c.${DatabaseHelper.COL_CAT_ID}
                WHERE e.${DatabaseHelper.COL_EXP_USER_ID} = $userId
                  AND c.${DatabaseHelper.COL_CAT_NAME} = '$catName'
                  AND ($monthFilter)
            """, null)

            var amount = 0f
            if (cursor.moveToFirst()) amount = cursor.getFloat(0)
            cursor.close()

            if (amount > 0) {
                entries.add(BarEntry(entries.size.toFloat(), amount))
                colors.add(barColor)
                labels.add(catName)
                totalSpent += amount
                if (amount > maxSpent) { maxSpent = amount; topCat = catName }
            }
        }

        if (entries.isEmpty()) {
            barChart.clear()
            barChart.setNoDataText("No expenses for this period")
            tvInsight.text = "No expenses logged yet. Start tracking to see insights!"
            return
        }

        val dataSet = BarDataSet(entries, "Spending").apply {
            this.colors = colors
            valueTextSize = 10f
            valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float) = "R${value.toInt()}"
            }
        }

        // Min/Max goal lines based on monthly budget
        val monthlyBudget = prefs.getFloat("monthly_budget", 4000f)
        val perCatBudget  = monthlyBudget / categories.size.coerceAtLeast(1)

        val minLine = LimitLine(perCatBudget * 0.3f, "Min Goal").apply {
            lineColor     = Color.parseColor("#2E7D32")
            lineWidth     = 1.5f
            textColor     = Color.parseColor("#2E7D32")
            textSize      = 10f
            enableDashedLine(10f, 5f, 0f)
        }
        val maxLine = LimitLine(perCatBudget * 1.5f, "Max Goal").apply {
            lineColor     = Color.parseColor("#C62828")
            lineWidth     = 1.5f
            textColor     = Color.parseColor("#C62828")
            textSize      = 10f
            enableDashedLine(10f, 5f, 0f)
        }

        barChart.apply {
            data = BarData(dataSet).apply { barWidth = 0.6f }
            description.isEnabled = false
            legend.isEnabled      = false
            setFitBars(true)
            setPinchZoom(false)
            setDrawGridBackground(false)
            animateY(800)

            axisLeft.apply {
                removeAllLimitLines()
                addLimitLine(minLine)
                addLimitLine(maxLine)
                axisMinimum = 0f
                gridColor   = Color.parseColor("#E0E0E0")
                textColor   = Color.parseColor("#757575")
            }
            axisRight.isEnabled = false

            xAxis.apply {
                valueFormatter     = IndexAxisValueFormatter(labels)
                position           = XAxis.XAxisPosition.BOTTOM
                granularity        = 1f
                setDrawGridLines(false)
                labelRotationAngle = -35f
                textColor          = Color.parseColor("#1A1A1A")
                textSize           = 9f
            }
            invalidate()
        }

        val remaining = monthlyBudget - totalSpent
        tvInsight.text = when {
            remaining < 0  -> "🔴 R${"%.0f".format(-remaining)} over budget! $topCat is your biggest spend."
            remaining < monthlyBudget * 0.1 -> "🟡 Almost at your limit — only R${"%.0f".format(remaining)} left."
            else -> "✅ $topCat is your biggest spend (R${"%.0f".format(maxSpent)}). R${"%.0f".format(remaining)} remaining."
        }
    }
}
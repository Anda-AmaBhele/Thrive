package com.yourname.thrive

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class ExpenseListActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_list)

        db = DatabaseHelper(this)
        prefs = getSharedPreferences("thrive_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        val listView      = findViewById<ListView>(R.id.listExpenses)
        val btnBack       = findViewById<ImageButton>(R.id.btnBack)
        val spinnerFilter = findViewById<Spinner>(R.id.spinnerFilter)
        val tvTotal       = findViewById<TextView>(R.id.tvTotal)

        val filters = listOf("This Month", "This Week", "Today", "All Time")
        spinnerFilter.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, filters).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                loadExpenses(userId, filters[pos], listView, tvTotal)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        loadExpenses(userId, "This Month", listView, tvTotal)
        btnBack.setOnClickListener { finish() }
    }

    private fun loadExpenses(userId: Int, filter: String, listView: ListView, tvTotal: TextView) {
        val sdf   = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        val month = today.substring(0, 7)
        val cal   = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val weekStart = sdf.format(cal.time)

        val whereDate = when (filter) {
            "Today"      -> "AND e.${DatabaseHelper.COL_EXP_DATE} = '$today'"
            "This Week"  -> "AND e.${DatabaseHelper.COL_EXP_DATE} >= '$weekStart'"
            "This Month" -> "AND e.${DatabaseHelper.COL_EXP_DATE} LIKE '$month%'"
            else         -> ""
        }

        val cursor = db.readableDatabase.rawQuery("""
            SELECT e.${DatabaseHelper.COL_EXP_AMOUNT},
                   e.${DatabaseHelper.COL_EXP_DESCRIPTION},
                   e.${DatabaseHelper.COL_EXP_DATE},
                   c.${DatabaseHelper.COL_CAT_NAME}
            FROM ${DatabaseHelper.TABLE_EXPENSES} e
            JOIN ${DatabaseHelper.TABLE_CATEGORIES} c
              ON e.${DatabaseHelper.COL_EXP_CATEGORY_ID} = c.${DatabaseHelper.COL_CAT_ID}
            WHERE e.${DatabaseHelper.COL_EXP_USER_ID} = $userId $whereDate
            ORDER BY e.${DatabaseHelper.COL_EXP_DATE} DESC
        """, null)

        val items  = mutableListOf<String>()
        var total  = 0.0
        while (cursor.moveToNext()) {
            val amount = cursor.getDouble(0)
            val desc   = cursor.getString(1)?.takeIf { it.isNotEmpty() } ?: "No description"
            val date   = cursor.getString(2)
            val cat    = cursor.getString(3)
            total += amount
            items.add("R%.2f  —  %s\n📅 %s  |  🏷 %s".format(amount, desc, date, cat))
        }
        cursor.close()

        if (items.isEmpty()) items.add("No expenses found for this period.")
        tvTotal.text = "Total: R%.2f".format(total)
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
    }
}
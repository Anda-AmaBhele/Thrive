package com.yourname.thrive

import android.content.ContentValues
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var prefs: SharedPreferences
    private var selectedCategoryId: Int = -1
    private val categoryIds = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        db = DatabaseHelper(this)
        prefs = getSharedPreferences("thrive_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        val etAmount      = findViewById<EditText>(R.id.etAmount)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etDate        = findViewById<EditText>(R.id.etDate)
        val spinnerCat    = findViewById<Spinner>(R.id.spinnerCategory)
        val btnSave       = findViewById<Button>(R.id.btnSave)
        val btnBack       = findViewById<ImageButton>(R.id.btnBack)

        // Set today's date
        etDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))

        // Load categories
        val cursor = db.readableDatabase.query(
            DatabaseHelper.TABLE_CATEGORIES, null,
            "${DatabaseHelper.COL_CAT_USER_ID}=?",
            arrayOf(userId.toString()), null, null,
            "${DatabaseHelper.COL_CAT_NAME} ASC"
        )
        val categoryNames = mutableListOf<String>()
        while (cursor.moveToNext()) {
            categoryNames.add(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_NAME)))
            categoryIds.add(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_ID)))
        }
        cursor.close()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCat.adapter = adapter

        spinnerCat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                selectedCategoryId = categoryIds[pos]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnSave.setOnClickListener {

            setContentView(R.layout.activity_add_expense)
            val amountStr   = etAmount.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val date        = etDate.text.toString().trim()

            // Validation
            when {
                amountStr.isEmpty() -> {
                    etAmount.error = "Please enter an amount"
                    return@setOnClickListener
                }
                amountStr.toDoubleOrNull() == null || amountStr.toDouble() <= 0 -> {
                    etAmount.error = "Please enter a valid amount"
                    return@setOnClickListener
                }
                date.isEmpty() -> {
                    etDate.error = "Please enter a date"
                    return@setOnClickListener
                }
                selectedCategoryId == -1 -> {
                    Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val values = ContentValues().apply {
                put(DatabaseHelper.COL_EXP_AMOUNT,      amountStr.toDouble())
                put(DatabaseHelper.COL_EXP_DESCRIPTION, description)
                put(DatabaseHelper.COL_EXP_DATE,        date)
                put(DatabaseHelper.COL_EXP_CATEGORY_ID, selectedCategoryId)
                put(DatabaseHelper.COL_EXP_USER_ID,     userId)
            }

            val result = db.writableDatabase.insert(DatabaseHelper.TABLE_EXPENSES, null, values)
            if (result != -1L) {
                Toast.makeText(this, "✅ Expense saved!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error saving expense", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener { finish() }
    }
}
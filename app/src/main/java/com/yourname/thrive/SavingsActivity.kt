package com.yourname.thrive

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class SavingsActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var prefs: SharedPreferences
    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            findViewById<ImageView>(R.id.imgGoalPreview).apply {
                setImageURI(it)
                visibility = android.view.View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_savings)

        db = DatabaseHelper(this)
        prefs = getSharedPreferences("thrive_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        createNotificationChannel()

        val etName      = findViewById<EditText>(R.id.etGoalName)
        val etTarget    = findViewById<EditText>(R.id.etTargetAmount)
        val etDate      = findViewById<EditText>(R.id.etTargetDate)
        val btnSave     = findViewById<Button>(R.id.btnSaveGoal)
        val listView    = findViewById<ListView>(R.id.listSavingsGoals)
        val btnBack     = findViewById<ImageButton>(R.id.btnBack)
        val btnAddPhoto = findViewById<Button>(R.id.btnAddPhoto)

        etDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))

        btnAddPhoto.setOnClickListener { pickImage.launch("image/*") }

        loadGoals(userId, listView)

        btnSave.setOnClickListener {
            val name   = etName.text.toString().trim()
            val target = etTarget.text.toString().trim()
            val date   = etDate.text.toString().trim()

            when {
                name.isEmpty()   -> { etName.error   = "Enter goal name"; return@setOnClickListener }
                target.isEmpty() -> { etTarget.error = "Enter amount"; return@setOnClickListener }
                target.toDoubleOrNull() == null || target.toDouble() <= 0 -> {
                    etTarget.error = "Enter valid amount"; return@setOnClickListener }
                date.isEmpty()   -> { etDate.error   = "Enter date"; return@setOnClickListener }
            }

            val values = ContentValues().apply {
                put(DatabaseHelper.COL_SAV_USER_ID, userId)
                put(DatabaseHelper.COL_SAV_NAME,    name)
                put(DatabaseHelper.COL_SAV_TARGET,  target.toDouble())
                put(DatabaseHelper.COL_SAV_DATE,    date)
                put(DatabaseHelper.COL_SAV_SAVED,   0.0)
            }
            db.writableDatabase.insert(DatabaseHelper.TABLE_SAVINGS, null, values)
            scheduleReminder(name, date)
            Toast.makeText(this, "✅ Goal added! Reminder set for $date", Toast.LENGTH_LONG).show()
            etName.text.clear()
            etTarget.text.clear()
            selectedImageUri = null
            findViewById<ImageView>(R.id.imgGoalPreview).visibility = android.view.View.GONE
            loadGoals(userId, listView)
        }

        listView.setOnItemLongClickListener { _, _, pos, _ ->
            val cursor = db.readableDatabase.query(
                DatabaseHelper.TABLE_SAVINGS, null,
                "${DatabaseHelper.COL_SAV_USER_ID}=?",
                arrayOf(userId.toString()), null, null, null
            )
            val ids = mutableListOf<Int>()
            while (cursor.moveToNext()) {
                ids.add(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SAV_ID)))
            }
            cursor.close()

            if (pos < ids.size) {
                AlertDialog.Builder(this)
                    .setTitle("Delete Goal")
                    .setMessage("Are you sure you want to delete this goal?")
                    .setPositiveButton("Delete") { _, _ ->
                        db.writableDatabase.delete(
                            DatabaseHelper.TABLE_SAVINGS,
                            "${DatabaseHelper.COL_SAV_ID}=?",
                            arrayOf(ids[pos].toString())
                        )
                        Toast.makeText(this, "Goal deleted", Toast.LENGTH_SHORT).show()
                        loadGoals(userId, listView)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            true
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun loadGoals(userId: Int, listView: ListView) {
        val cursor = db.readableDatabase.query(
            DatabaseHelper.TABLE_SAVINGS, null,
            "${DatabaseHelper.COL_SAV_USER_ID}=?",
            arrayOf(userId.toString()), null, null, null
        )
        val items = mutableListOf<String>()
        while (cursor.moveToNext()) {
            val name   = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SAV_NAME))
            val target = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SAV_TARGET))
            val saved  = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SAV_SAVED))
            val date   = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SAV_DATE))
            val pct    = if (target > 0) (saved / target * 100).toInt() else 0
            items.add("🎯 $name\nR%.2f / R%.2f ($pct%%)  •  By $date\n⚠️ Long press to delete".format(saved, target))
        }
        cursor.close()
        if (items.isEmpty()) items.add("No goals yet. Add one above!")
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
    }

    private fun scheduleReminder(goalName: String, dateStr: String) {
        try {
            val sdf  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: return
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, ReminderReceiver::class.java).apply {
                putExtra("goal_name", goalName)
            }
            val pending = PendingIntent.getBroadcast(
                this, goalName.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.set(AlarmManager.RTC_WAKEUP, date.time, pending)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "thrive_channel", "Thrive Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Savings goal reminders" }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }
}
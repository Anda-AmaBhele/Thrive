package com.yourname.thrive

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val goalName = intent.getStringExtra("goal_name") ?: "Savings Goal"
        val notification = NotificationCompat.Builder(context, "thrive_reminders")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🎯 Thrive Reminder")
            .setContentText("Time to check on your goal: $goalName!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(goalName.hashCode(), notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
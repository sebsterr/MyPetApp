package com.example.mypetapp

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.Calendar

class FeedingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val petName = intent.getStringExtra("petName") ?: "Your pet"
        val foodType = intent.getStringExtra("foodType") ?: "food"
        val taskType = intent.getStringExtra("taskType") ?: "Feeding"
        val taskId = intent.getStringExtra("taskId") ?: ""

        val notificationTitle = intent.getStringExtra("title") ?: "Feeding time for $petName!"
        val notificationMessage = intent.getStringExtra("message") ?: "It's time for $foodType."

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "PET_CARE_CHANNEL"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pet Care Schedule",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for feeding, vaccines, and vet visits"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notificationTitle)
            .setContentText(notificationMessage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)

        if (taskType == "Feeding" && taskId.isNotEmpty()) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val nextIntent = Intent(context, FeedingReceiver::class.java).apply {
                putExtra("petName", petName)
                putExtra("title", notificationTitle)
                putExtra("message", notificationMessage)
                putExtra("foodType", foodType)
                putExtra("taskType", taskType)
                putExtra("taskId", taskId)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId.hashCode(),
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_YEAR, 1)
            }

            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
}
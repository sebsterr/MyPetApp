package com.example.mypetapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class FeedingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val petName = intent.getStringExtra("petName") ?: "Your pet"
        val foodType = intent.getStringExtra("foodType") ?: "food"


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
    }
}
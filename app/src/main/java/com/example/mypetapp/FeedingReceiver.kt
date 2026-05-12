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
        val petName = intent.getStringExtra("petName") ?: "Animalul tău"
        val foodType = intent.getStringExtra("foodType") ?: "mâncare"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "feeding_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Program Hrănire",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificări pentru ora de masă a animalelor"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Oră de masă pentru $petName!")
            .setContentText("Este timpul pentru porția de $foodType.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
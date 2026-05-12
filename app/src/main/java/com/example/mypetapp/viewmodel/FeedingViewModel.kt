package com.example.mypetapp.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.mypetapp.FeedingReceiver
import com.example.mypetapp.screens.FeedingTask
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*
import android.os.Build
class FeedingViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _schedule = MutableStateFlow<List<FeedingTask>>(emptyList())
    val schedule: StateFlow<List<FeedingTask>> = _schedule

    fun getFeedingSchedule(petId: String) {
        db.collection("pets").document(petId).collection("feeding_schedule")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _schedule.value = snapshot.toObjects(FeedingTask::class.java)
                }
            }
    }

    fun saveFeedingTask(context: Context, petId: String, petName: String, time: String, food: String, quantity: String) {
        val taskId = db.collection("pets").document(petId).collection("feeding_schedule").document().id
        val task = FeedingTask(taskId, petId, time, food, quantity)

        db.collection("pets").document(petId)
            .collection("feeding_schedule")
            .document(taskId)
            .set(task)
            .addOnSuccessListener {
                scheduleAlarm(context, task, petName)
            }
    }
    fun deleteFeedingTask(petId: String, taskId: String) {
        db.collection("pets").document(petId)
            .collection("feeding_schedule")
            .document(taskId)
            .delete()
    }
    private fun scheduleAlarm(context: Context, task: FeedingTask, petName: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent().apply {
                    action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                }
                context.startActivity(intent)
                return
            }
        }

        val intent = Intent(context, FeedingReceiver::class.java).apply {
            putExtra("petName", petName)
            putExtra("foodType", task.foodType)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            val parts = task.time.split(":")
            set(Calendar.HOUR_OF_DAY, parts[0].toInt())
            set(Calendar.MINUTE, parts[1].toInt())
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DATE, 1)
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
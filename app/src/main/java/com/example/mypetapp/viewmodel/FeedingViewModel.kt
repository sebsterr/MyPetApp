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
import java.text.SimpleDateFormat

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

    fun saveFeedingTask(
        context: Context,
        petId: String,
        petName: String,
        time: String,
        food: String,
        quantity: String,
        type: String = "Feeding",
        date: String = "",
        recurrence: String = "None",
        existingTaskId: String? = null
    ) {
        val taskId = existingTaskId ?: db.collection("pets").document(petId).collection("feeding_schedule").document().id

        val task = FeedingTask(
            id = taskId,
            petId = petId,
            time = time,
            foodType = food,
            quantity = quantity,
            type = type,
            date = date,
            recurrence = recurrence
        )

        db.collection("pets").document(petId)
            .collection("feeding_schedule")
            .document(taskId)
            .set(task)
            .addOnSuccessListener {
                scheduleAlarm(context, task, petName)
            }
    }

    fun deleteFeedingTask(context: Context, petId: String, taskId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, FeedingReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)

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

        val customTitle = when (task.type) {
            "Visit" -> "Vet Appointment Tomorrow!"
            "Vaccine" -> "Upcoming Vaccination Reminder"
            else -> "Feeding Time for $petName"
        }

        val customMessage = when (task.type) {
            "Visit" -> "Reminder: Tomorrow you have a doctor visit with $petName for '${task.foodType}'."
            "Vaccine" -> "Reminder: Tomorrow $petName needs the vaccine: '${task.foodType}'."
            else -> "It's time to feed $petName: ${task.foodType}."
        }

        val intent = Intent(context, FeedingReceiver::class.java).apply {
            putExtra("petName", petName)
            putExtra("title", customTitle)
            putExtra("message", customMessage)
            putExtra("foodType", task.foodType)
            putExtra("taskType", task.type)
            putExtra("taskId", task.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sdf = SimpleDateFormat("yyyy-M-d HH:mm", Locale.getDefault())
        val timeToParse = task.time.ifBlank { "09:00" }

        val dateToParse = if (task.type == "Feeding") {
            val today = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(Date())
            "$today $timeToParse"
        } else {
            "${task.date} $timeToParse"
        }

        val parsedDate = sdf.parse(dateToParse) ?: return
        val calendar = Calendar.getInstance().apply { time = parsedDate }


        when (task.type) {
            "Visit" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }
            "Vaccine" -> {

                if (task.recurrence == "6 Months" || task.recurrence == "1 Year") {
                    calendar.add(Calendar.DAY_OF_YEAR, -7)
                } else {
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                }
            }
            "Feeding" -> {
                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DATE, 1)
                }
            }
        }

            if (calendar.timeInMillis < System.currentTimeMillis()) return

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
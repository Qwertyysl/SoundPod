package com.github.soundxflow.azan

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import androidx.work.*
import com.github.soundxflow.utils.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AzanWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val preferences = applicationContext.preferences
        val azanEnabled = preferences.getBoolean(azanReminderEnabledKey, false)
        if (!azanEnabled) return Result.success()

        val zone = preferences.getString(azanLocationKey, "WLY01") ?: "WLY01"
        val response = JakimApi.getPrayerTimes(zone) ?: return Result.retry()

        val prayerTime = response.prayerTime.firstOrNull() ?: return Result.failure()

        // Force update UI and Alarms with latest data
        preferences.edit {
            putString(prayerTimesTodayKey, Json.encodeToString(prayerTime))
        }

        scheduleAlarms(prayerTime)

        return Result.success()
    }

    private fun scheduleAlarms(prayerTime: PrayerTime) {
        val times = listOfNotNull(
            prayerTime.fajr,
            prayerTime.dhuhr,
            prayerTime.asr,
            prayerTime.maghrib,
            prayerTime.isha
        )

        times.forEachIndexed { index, time ->
            scheduleAlarmForPrayer(time, index)
        }
    }

    private fun scheduleAlarmForPrayer(timeStr: String, prayerId: Int) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val prayerDate = sdf.parse(timeStr) ?: return
        
        val calendar = Calendar.getInstance()
        val prayerCalendar = Calendar.getInstance().apply {
            time = prayerDate
            set(Calendar.YEAR, calendar.get(Calendar.YEAR))
            set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
        }

        if (prayerCalendar.before(calendar)) return // Already passed today

        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Alarm for 5 seconds before
        val intentBefore = Intent(applicationContext, AzanReceiver::class.java).apply {
            action = "com.github.soundxflow.azan.PAUSE"
            putExtra("PRAYER_ID", prayerId)
        }
        val pendingIntentBefore = PendingIntent.getBroadcast(
            applicationContext,
            prayerId * 10 + 1,
            intentBefore,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            prayerCalendar.timeInMillis - 5000,
            pendingIntentBefore
        )

        // Alarm for Azan
        val intentAzan = Intent(applicationContext, AzanReceiver::class.java).apply {
            action = "com.github.soundxflow.azan.PLAY"
            putExtra("PRAYER_ID", prayerId)
        }
        val pendingIntentAzan = PendingIntent.getBroadcast(
            applicationContext,
            prayerId * 10 + 2,
            intentAzan,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            prayerCalendar.timeInMillis,
            pendingIntentAzan
        )
    }

    companion object {
        fun enqueue(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<AzanWorker>(1, TimeUnit.DAYS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "AzanWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
        
        fun runOnce(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<AzanWorker>().build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}

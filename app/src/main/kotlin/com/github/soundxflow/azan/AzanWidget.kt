package com.github.soundxflow.azan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.soundxflow.utils.azanLocationKey
import com.github.soundxflow.utils.azanReminderEnabledKey
import com.github.soundxflow.utils.prayerTimesTodayKey
import com.github.soundxflow.utils.rememberPreference
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AzanWidget() {
    val context = LocalContext.current
    val azanEnabled by rememberPreference(azanReminderEnabledKey, false)
    val selectedZone by rememberPreference(azanLocationKey, "WLY01")
    var prayerTimesJson by rememberPreference(prayerTimesTodayKey, "")

    if (!azanEnabled) return

    val prayerTime = remember(prayerTimesJson) {
        try {
            if (prayerTimesJson.isNotEmpty()) {
                Json.decodeFromString<PrayerTime>(prayerTimesJson)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    LaunchedEffect(selectedZone, today) {
        // Force refresh from API when zone changes, date changes, or if data is missing
        val response = JakimApi.getPrayerTimes(selectedZone)
        response?.prayerTime?.firstOrNull()?.let {
            prayerTimesJson = Json.encodeToString(it)
        }
    }

    if (prayerTime == null) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("Loading prayer times for $selectedZone...")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Prayer Times (${azanZones.find { it.code == selectedZone }?.code})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PrayerTimeItem("Subuh", format12h(prayerTime.fajr ?: ""))
            PrayerTimeItem("Zohor", format12h(prayerTime.dhuhr ?: ""))
            PrayerTimeItem("Asar", format12h(prayerTime.asr ?: ""))
            PrayerTimeItem("Maghrib", format12h(prayerTime.maghrib ?: ""))
            PrayerTimeItem("Isyak", format12h(prayerTime.isha ?: ""))
        }
    }
}

@Composable
fun PrayerTimeItem(name: String, time: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = name, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
        Text(text = time, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

fun format12h(time24: String): String {
    return try {
        val sdf24 = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val sdf12 = SimpleDateFormat("h:mm a", Locale.getDefault())
        val date = sdf24.parse(time24)
        sdf12.format(date!!)
    } catch (e: Exception) {
        time24
    }
}

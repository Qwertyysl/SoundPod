package com.github.soundxflow.ui.screens.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.soundxflow.azan.AzanZone
import com.github.soundxflow.azan.AzanWorker
import com.github.soundxflow.azan.azanZones
import com.github.soundxflow.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzanSettings(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var azanEnabled by rememberPreference(azanReminderEnabledKey, false)
    var selectedZone by rememberPreference(azanLocationKey, "WLY01")
    var azanAudioPath by rememberPreference(azanAudioPathKey, "")

    var showZoneDialog by remember { mutableStateOf(false) }

    LaunchedEffect(azanEnabled, selectedZone) {
        if (azanEnabled) {
            AzanWorker.runOnce(context)
            AzanWorker.enqueue(context)
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                azanAudioPath = it.toString()
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Azan Reminder") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SettingColum(
                title = "Enable Azan Reminder",
                description = "Pause music and play Azan during prayer times",
                trailingContent = {
                    Switch(checked = azanEnabled, onCheckedChange = { azanEnabled = it })
                },
                onClick = { azanEnabled = !azanEnabled }
            )

            SettingColum(
                title = "Location (Zone)",
                description = azanZones.find { it.code == selectedZone }?.name ?: selectedZone,
                onClick = { showZoneDialog = true }
            )

            SettingColum(
                title = "Azan Audio",
                description = if (azanAudioPath.isEmpty()) "Not selected" else "Selected: ${Uri.parse(azanAudioPath).lastPathSegment}",
                onClick = {
                    audioPickerLauncher.launch(arrayOf("audio/*"))
                }
            )
        }
    }

    if (showZoneDialog) {
        AlertDialog(
            onDismissRequest = { showZoneDialog = false },
            title = { Text("Select Zone") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(azanZones) { zone ->
                        Text(
                            text = "${zone.code} - ${zone.name}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedZone = zone.code
                                    showZoneDialog = false
                                }
                                .padding(16.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showZoneDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}


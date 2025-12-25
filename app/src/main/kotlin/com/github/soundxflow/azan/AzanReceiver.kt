package com.github.soundxflow.azan

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AzanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.github.soundxflow.azan.PAUSE" -> {
                val pauseIntent = Intent("com.github.soundxflow.pause").apply {
                    setPackage(context.packageName)
                }
                context.sendBroadcast(pauseIntent)
            }
            "com.github.soundxflow.azan.PLAY" -> {
                val serviceIntent = Intent(context, AzanService::class.java).apply {
                    action = "PLAY_AZAN"
                }
                context.startService(serviceIntent)
            }
        }
    }
}

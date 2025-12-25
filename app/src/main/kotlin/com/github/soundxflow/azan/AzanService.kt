package com.github.soundxflow.azan

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import com.github.soundxflow.utils.azanAudioPathKey
import com.github.soundxflow.utils.preferences
import kotlinx.coroutines.*

class AzanService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == "PLAY_AZAN") {
            playAzan()
        }
        return START_NOT_STICKY
    }

    private fun playAzan() {
        val audioPath = preferences.getString(azanAudioPathKey, "")
        
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                if (audioPath.isNullOrEmpty()) {
                    val assetFileDescriptor = resources.openRawResourceFd(com.github.soundxflow.R.raw.azan)
                    setDataSource(assetFileDescriptor.fileDescriptor, assetFileDescriptor.startOffset, assetFileDescriptor.length)
                    assetFileDescriptor.close()
                } else {
                    setDataSource(applicationContext, Uri.parse(audioPath))
                }
                prepare()
                start()
                setOnCompletionListener {
                    scope.launch {
                        delay(15000)
                        resumePlayer()
                        stopSelf()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback for missing resource or error
            if (audioPath.isNullOrEmpty()) {
                 scope.launch {
                    delay(60000) // Simulate azan duration
                    resumePlayer()
                    stopSelf()
                }
            } else {
                resumePlayer()
                stopSelf()
            }
        }
    }

    private fun resumePlayer() {
        val intent = Intent("com.github.soundxflow.play").apply {
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        scope.cancel()
        super.onDestroy()
    }
}

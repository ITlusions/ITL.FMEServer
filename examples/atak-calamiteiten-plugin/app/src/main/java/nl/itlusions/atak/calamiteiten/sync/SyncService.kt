package nl.itlusions.atak.calamiteiten.sync

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import nl.itlusions.atak.calamiteiten.data.CalamiteitenRepository
import android.util.Log

/**
 * Foreground service voor manuele sync operaties
 * 
 * Toont notification tijdens sync en update real-time progress.
 * Gebruikt voor gebruiker-geïnitieerde sync (refresh button).
 */
class SyncService : Service() {
    
    private val repository = CalamiteitenRepository.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var syncJob: Job? = null
    
    companion object {
        private const val TAG = "SyncService"
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "calamiteiten_sync_channel"
        private const val CHANNEL_NAME = "Calamiteiten Sync"
        
        const val ACTION_START_SYNC = "nl.itlusions.atak.calamiteiten.ACTION_START_SYNC"
        const val ACTION_STOP_SYNC = "nl.itlusions.atak.calamiteiten.ACTION_STOP_SYNC"
        
        fun startSync(context: Context) {
            val intent = Intent(context, SyncService::class.java).apply {
                action = ACTION_START_SYNC
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopSync(context: Context) {
            val intent = Intent(context, SyncService::class.java).apply {
                action = ACTION_STOP_SYNC
            }
            context.startService(intent)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SYNC -> {
                Log.i(TAG, "Starting sync")
                startForeground(NOTIFICATION_ID, createNotification("Synchroniseren..."))
                startSync()
            }
            ACTION_STOP_SYNC -> {
                Log.i(TAG, "Stopping sync")
                stopSync()
            }
        }
        return START_NOT_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    /**
     * Start sync operatie
     */
    private fun startSync() {
        syncJob?.cancel()
        
        syncJob = scope.launch {
            try {
                updateNotification("Ophalen brandweerkazernes...")
                delay(100) // Kort delay voor UI update
                
                updateNotification("Ophalen noodopvanglocaties...")
                delay(100)
                
                // Voer sync uit
                val result = repository.syncData()
                
                if (result.success) {
                    Log.i(TAG, "Sync successful: ${result.message}")
                    showSuccessNotification(result.message)
                } else {
                    Log.w(TAG, "Sync failed: ${result.message}")
                    showErrorNotification(result.message)
                }
                
            } catch (e: CancellationException) {
                Log.i(TAG, "Sync cancelled")
                showCancelledNotification()
            } catch (e: Exception) {
                Log.e(TAG, "Sync error", e)
                showErrorNotification("Sync fout: ${e.message}")
            } finally {
                // Stop service na completion
                stopForeground(true)
                stopSelf()
            }
        }
    }
    
    /**
     * Stop sync operatie
     */
    private fun stopSync() {
        syncJob?.cancel()
        stopForeground(true)
        stopSelf()
    }
    
    /**
     * Create notification channel (Android O+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaties voor calamiteiten data synchronisatie"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Create basis notification
     */
    private fun createNotification(text: String): Notification {
        val stopIntent = Intent(this, SyncService::class.java).apply {
            action = ACTION_STOP_SYNC
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Calamiteiten Data")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .setProgress(0, 0, true) // Indeterminate progress
            .addAction(
                android.R.drawable.ic_delete,
                "Stop",
                stopPendingIntent
            )
            .build()
    }
    
    /**
     * Update notification text
     */
    private fun updateNotification(text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(text))
    }
    
    /**
     * Toon success notification
     */
    private fun showSuccessNotification(message: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sync Voltooid")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }
    
    /**
     * Toon error notification
     */
    private fun showErrorNotification(message: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sync Mislukt")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID + 2, notification)
    }
    
    /**
     * Toon cancelled notification
     */
    private fun showCancelledNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sync Geannuleerd")
            .setContentText("Synchronisatie gestopt door gebruiker")
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID + 3, notification)
    }
    
    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        syncJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }
}

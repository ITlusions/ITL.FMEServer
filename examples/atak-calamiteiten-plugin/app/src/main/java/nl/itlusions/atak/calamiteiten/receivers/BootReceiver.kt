package nl.itlusions.atak.calamiteiten.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import nl.itlusions.atak.calamiteiten.sync.SyncManager

/**
 * Boot Receiver om sync te starten na device reboot
 * 
 * Zorgt ervoor dat periodic sync automatisch hervat wordt
 * wanneer het apparaat opstart. Belangrijk voor offline-first
 * gebruik waarbij data altijd actueel moet blijven.
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            Log.w(TAG, "Received null context or intent")
            return
        }
        
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.i(TAG, "Boot completed or package replaced, starting sync")
                
                try {
                    // Check of sync enabled is in preferences
                    val prefs = context.getSharedPreferences("calamiteiten_prefs", Context.MODE_PRIVATE)
                    val syncEnabled = prefs.getBoolean("sync_enabled", true)
                    
                    if (syncEnabled) {
                        // Start periodic sync met configured interval
                        val syncInterval = prefs.getLong("sync_interval_minutes", 15L)
                        
                        val syncManager = SyncManager.getInstance(context)
                        syncManager.startPeriodicSync(syncInterval)
                        
                        Log.i(TAG, "Periodic sync started with interval: $syncInterval minutes")
                    } else {
                        Log.i(TAG, "Sync is disabled in settings, not starting")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting sync after boot", e)
                }
            }
            else -> {
                Log.d(TAG, "Received action: ${intent.action}")
            }
        }
    }
}

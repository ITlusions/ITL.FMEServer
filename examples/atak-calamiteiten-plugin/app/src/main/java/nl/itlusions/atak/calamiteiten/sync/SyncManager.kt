package nl.itlusions.atak.calamiteiten.sync

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.*
import nl.itlusions.atak.calamiteiten.data.CalamiteitenRepository
import java.util.concurrent.TimeUnit
import android.util.Log

/**
 * Sync Manager voor periodieke synchronisatie van calamiteiten data
 * 
 * Gebruikt WorkManager voor betrouwbare achtergrond sync:
 * - Periodic sync elke 15 minuten (configureerbaar)
 * - One-time sync voor directe updates
 * - Exponential backoff bij fouten
 * - Battery-aware scheduling
 */
class SyncManager private constructor(private val context: Context) {
    
    private val repository = CalamiteitenRepository.getInstance()
    
    companion object {
        private const val TAG = "SyncManager"
        private const val PERIODIC_SYNC_WORK = "calamiteiten_periodic_sync"
        private const val ONE_TIME_SYNC_WORK = "calamiteiten_onetime_sync"
        
        // Default sync interval: 15 minuten
        private const val SYNC_INTERVAL_MINUTES = 15L
        
        @Volatile
        private var instance: SyncManager? = null
        
        fun getInstance(context: Context): SyncManager {
            return instance ?: synchronized(this) {
                instance ?: SyncManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Start periodic sync
     */
    fun startPeriodicSync(intervalMinutes: Long = SYNC_INTERVAL_MINUTES) {
        Log.i(TAG, "Starting periodic sync with interval: $intervalMinutes minutes")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            intervalMinutes, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES // Flex interval: run within 5 minutes of scheduled time
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag("periodic")
            .addTag("calamiteiten")
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )
    }
    
    /**
     * Stop periodic sync
     */
    fun stopPeriodicSync() {
        Log.i(TAG, "Stopping periodic sync")
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_SYNC_WORK)
    }
    
    /**
     * Trigger immediate one-time sync
     */
    fun syncNow(): LiveData<WorkInfo> {
        Log.i(TAG, "Triggering immediate sync")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val oneTimeSyncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("onetime")
            .addTag("calamiteiten")
            .build()
        
        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_TIME_SYNC_WORK,
            ExistingWorkPolicy.REPLACE,
            oneTimeSyncRequest
        )
        
        return WorkManager.getInstance(context).getWorkInfoByIdLiveData(oneTimeSyncRequest.id)
    }
    
    /**
     * Check of sync actief is
     */
    fun isSyncActive(): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(PERIODIC_SYNC_WORK)
            .get()
        
        return workInfos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
    }
    
    /**
     * Get laatste sync status
     */
    fun getLastSyncStatus(): LiveData<List<WorkInfo>> {
        return WorkManager.getInstance(context)
            .getWorkInfosByTagLiveData("calamiteiten")
    }
    
    /**
     * Cancel all sync work
     */
    fun cancelAllSync() {
        Log.i(TAG, "Cancelling all sync work")
        WorkManager.getInstance(context).cancelAllWorkByTag("calamiteiten")
    }
}

/**
 * Worker class voor sync operations
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val repository = CalamiteitenRepository.getInstance()
    
    companion object {
        private const val TAG = "SyncWorker"
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Starting sync operation")
            
            // Toon notification voor foreground work
            setForeground(createForegroundInfo())
            
            // Sync alle data
            val result = repository.syncData()
            
            if (result.success) {
                Log.i(TAG, "Sync completed successfully: ${result.message}")
                
                // Update output data
                val outputData = workDataOf(
                    "success" to true,
                    "message" to result.message,
                    "kazernes_count" to result.kazernesCount,
                    "noodopvang_count" to result.noodopvangCount,
                    "timestamp" to System.currentTimeMillis()
                )
                
                Result.success(outputData)
            } else {
                Log.w(TAG, "Sync failed: ${result.message}")
                
                val outputData = workDataOf(
                    "success" to false,
                    "message" to result.message,
                    "timestamp" to System.currentTimeMillis()
                )
                
                // Retry met exponential backoff
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync error", e)
            
            val outputData = workDataOf(
                "success" to false,
                "message" to "Sync fout: ${e.message}",
                "timestamp" to System.currentTimeMillis()
            )
            
            Result.failure(outputData)
        }
    }
    
    /**
     * Create foreground notification
     */
    private fun createForegroundInfo(): ForegroundInfo {
        val notification = android.app.NotificationCompat.Builder(
            applicationContext,
            "calamiteiten_sync"
        )
            .setContentTitle("Calamiteiten Data Sync")
            .setContentText("Synchroniseren van noodvoorzieningen...")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .build()
        
        return ForegroundInfo(
            1001, // Notification ID
            notification
        )
    }
}

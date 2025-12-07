package nl.itlusions.atak.calamiteiten.ui

import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.preference.EditTextPreference
import android.preference.SwitchPreference
import android.preference.ListPreference
import android.util.Log
import nl.itlusions.atak.calamiteiten.R
import nl.itlusions.atak.calamiteiten.sync.SyncManager

/**
 * Settings Activity voor plugin configuratie
 * 
 * Instellingen:
 * - API Endpoint URL
 * - API Key
 * - Sync interval
 * - Auto-sync enable/disable
 * - Offline mode
 * - Notification preferences
 */
class SettingsActivity : PreferenceActivity() {
    
    companion object {
        private const val TAG = "SettingsActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Laad preferences fragment
        fragmentManager.beginTransaction()
            .replace(android.R.id.content, SettingsFragment())
            .commit()
    }
    
    /**
     * Fragment voor preferences UI
     */
    class SettingsFragment : PreferenceFragment() {
        
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            
            // Laad preferences from XML
            addPreferencesFromResource(R.xml.preferences)
            
            setupApiEndpoint()
            setupApiKey()
            setupSyncInterval()
            setupAutoSync()
            setupOfflineMode()
            setupNotifications()
        }
        
        /**
         * API Endpoint configuratie
         */
        private fun setupApiEndpoint() {
            val endpointPref = findPreference("api_endpoint") as? EditTextPreference
            endpointPref?.apply {
                summary = text ?: "https://fme.itlusions.nl/api/v1/calamiteiten"
                
                setOnPreferenceChangeListener { _, newValue ->
                    val url = newValue.toString()
                    
                    // Valideer URL format
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        summary = url
                        Log.i(TAG, "API endpoint updated: $url")
                        true
                    } else {
                        Log.w(TAG, "Invalid API endpoint URL: $url")
                        false
                    }
                }
            }
        }
        
        /**
         * API Key configuratie
         */
        private fun setupApiKey() {
            val apiKeyPref = findPreference("api_key") as? EditTextPreference
            apiKeyPref?.apply {
                // Toon masked key in summary
                summary = if (text.isNullOrEmpty()) {
                    "Geen API key ingesteld"
                } else {
                    "••••••••${text.takeLast(4)}"
                }
                
                setOnPreferenceChangeListener { _, newValue ->
                    val key = newValue.toString()
                    
                    // Update masked summary
                    summary = if (key.isEmpty()) {
                        "Geen API key ingesteld"
                    } else {
                        "••••••••${key.takeLast(4)}"
                    }
                    
                    Log.i(TAG, "API key updated")
                    true
                }
            }
        }
        
        /**
         * Sync interval configuratie
         */
        private fun setupSyncInterval() {
            val intervalPref = findPreference("sync_interval_minutes") as? ListPreference
            intervalPref?.apply {
                // Options: 5, 15, 30, 60, 120, 240 minuten
                entries = arrayOf(
                    "5 minuten",
                    "15 minuten",
                    "30 minuten",
                    "1 uur",
                    "2 uur",
                    "4 uur"
                )
                entryValues = arrayOf("5", "15", "30", "60", "120", "240")
                
                summary = entry ?: "15 minuten"
                
                setOnPreferenceChangeListener { _, newValue ->
                    val minutes = newValue.toString().toLongOrNull() ?: 15L
                    
                    // Update sync manager
                    val syncManager = SyncManager.getInstance(activity.applicationContext)
                    syncManager.stopPeriodicSync()
                    syncManager.startPeriodicSync(minutes)
                    
                    // Update summary
                    val index = findIndexOfValue(newValue.toString())
                    summary = entries[index]
                    
                    Log.i(TAG, "Sync interval updated: $minutes minutes")
                    true
                }
            }
        }
        
        /**
         * Auto-sync enable/disable
         */
        private fun setupAutoSync() {
            val autoSyncPref = findPreference("sync_enabled") as? SwitchPreference
            autoSyncPref?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    val enabled = newValue as Boolean
                    
                    val syncManager = SyncManager.getInstance(activity.applicationContext)
                    
                    if (enabled) {
                        val prefs = preferenceManager.sharedPreferences
                        val interval = prefs.getLong("sync_interval_minutes", 15L)
                        syncManager.startPeriodicSync(interval)
                        Log.i(TAG, "Auto-sync enabled")
                    } else {
                        syncManager.stopPeriodicSync()
                        Log.i(TAG, "Auto-sync disabled")
                    }
                    
                    true
                }
            }
        }
        
        /**
         * Offline mode configuratie
         */
        private fun setupOfflineMode() {
            val offlineModePref = findPreference("offline_mode") as? SwitchPreference
            offlineModePref?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    val enabled = newValue as Boolean
                    
                    if (enabled) {
                        Log.i(TAG, "Offline mode enabled - using cached data only")
                        // Disable auto-sync in offline mode
                        val autoSyncPref = findPreference("sync_enabled") as? SwitchPreference
                        autoSyncPref?.isEnabled = false
                    } else {
                        Log.i(TAG, "Offline mode disabled - online data access")
                        // Re-enable auto-sync option
                        val autoSyncPref = findPreference("sync_enabled") as? SwitchPreference
                        autoSyncPref?.isEnabled = true
                    }
                    
                    true
                }
            }
        }
        
        /**
         * Notification preferences
         */
        private fun setupNotifications() {
            // Sync notifications
            val syncNotificationsPref = findPreference("notifications_sync") as? SwitchPreference
            syncNotificationsPref?.setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                Log.i(TAG, "Sync notifications: $enabled")
                true
            }
            
            // Alert notifications
            val alertNotificationsPref = findPreference("notifications_alerts") as? SwitchPreference
            alertNotificationsPref?.setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                Log.i(TAG, "Alert notifications: $enabled")
                true
            }
        }
    }
}

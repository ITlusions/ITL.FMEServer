package nl.itlusions.atak.calamiteiten

import android.content.Context
import android.content.Intent
import com.atak.plugins.impl.PluginLayoutInflater
import com.atakmap.android.dropdown.DropDownMapComponent
import com.atakmap.android.ipc.AtakBroadcast
import com.atakmap.android.maps.MapView
import com.atakmap.coremap.log.Log
import nl.itlusions.atak.calamiteiten.data.CalamiteitenRepository
import nl.itlusions.atak.calamiteiten.data.api.CalamiteitenApiService
import nl.itlusions.atak.calamiteiten.layers.BrandweerLayer
import nl.itlusions.atak.calamiteiten.layers.NoodopvangLayer
import nl.itlusions.atak.calamiteiten.sync.SyncManager

/**
 * Main plugin component for Calamiteiten ATAK Plugin
 * 
 * This plugin integrates Dutch emergency management data (brandweer/noodopvang)
 * into ATAK for situational awareness and emergency response.
 * 
 * Features:
 * - Brandweerkazernes layer (fire stations)
 * - Noodopvanglocaties layer (emergency shelters)
 * - Real-time sync with FME Server backend
 * - Offline mode with cached data
 * - Search and navigation
 * - Emergency notifications
 * 
 * @author ITLusions
 * @version 1.0.0
 */
class CalamiteitenMapComponent : DropDownMapComponent() {

    companion object {
        private const val TAG = "CalamiteitenPlugin"
        const val PLUGIN_VERSION = "1.0.0"
    }

    private lateinit var pluginContext: Context
    private lateinit var repository: CalamiteitenRepository
    private lateinit var syncManager: SyncManager
    private lateinit var brandweerLayer: BrandweerLayer
    private lateinit var noodopvangLayer: NoodopvangLayer

    override fun onCreate(
        context: Context,
        intent: Intent,
        mapView: MapView
    ) {
        context.setTheme(R.style.ATAKPluginTheme)
        super.onCreate(context, intent, mapView)

        pluginContext = context
        Log.d(TAG, "Calamiteiten Plugin v$PLUGIN_VERSION initializing...")

        try {
            // Initialize repository
            repository = CalamiteitenRepository.getInstance(context)
            Log.d(TAG, "Repository initialized")

            // Initialize sync manager
            syncManager = SyncManager(context, repository)
            syncManager.startPeriodicSync()
            Log.d(TAG, "Sync manager started")

            // Initialize map layers
            brandweerLayer = BrandweerLayer(context, mapView, repository)
            brandweerLayer.enable()
            Log.d(TAG, "Brandweer layer enabled")

            noodopvangLayer = NoodopvangLayer(context, mapView, repository)
            noodopvangLayer.enable()
            Log.d(TAG, "Noodopvang layer enabled")

            // Register plugin toolbar button
            registerToolbarButton()

            // Send plugin loaded broadcast
            sendPluginLoadedBroadcast()

            Log.i(TAG, "Calamiteiten Plugin successfully loaded")

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing plugin", e)
        }
    }

    override fun onDestroy(context: Context, mapView: MapView) {
        Log.d(TAG, "Calamiteiten Plugin shutting down...")

        try {
            // Stop sync
            syncManager.stopPeriodicSync()

            // Disable layers
            brandweerLayer.disable()
            noodopvangLayer.disable()

            Log.i(TAG, "Calamiteiten Plugin shutdown complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error during shutdown", e)
        }

        super.onDestroy(context, mapView)
    }

    /**
     * Register plugin button in ATAK toolbar
     */
    private fun registerToolbarButton() {
        val intent = Intent().apply {
            action = "nl.itlusions.atak.calamiteiten.SHOW_MENU"
        }
        
        AtakBroadcast.getInstance().registerReceiver(
            CalamiteitenMenuReceiver(pluginContext, mapView, repository),
            intent
        )

        Log.d(TAG, "Toolbar button registered")
    }

    /**
     * Send broadcast that plugin is loaded
     */
    private fun sendPluginLoadedBroadcast() {
        val intent = Intent("nl.itlusions.atak.calamiteiten.PLUGIN_LOADED").apply {
            putExtra("version", PLUGIN_VERSION)
        }
        AtakBroadcast.getInstance().sendBroadcast(intent)
    }
}

/**
 * Broadcast receiver for plugin menu actions
 */
class CalamiteitenMenuReceiver(
    private val context: Context,
    private val mapView: MapView,
    private val repository: CalamiteitenRepository
) : android.content.BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("CalamiteitenMenu", "Menu action received: ${intent.action}")
        
        when (intent.action) {
            "nl.itlusions.atak.calamiteiten.SHOW_MENU" -> {
                // Show main plugin menu
                showMainMenu()
            }
            "nl.itlusions.atak.calamiteiten.FIND_NEAREST" -> {
                // Find nearest resources
                val type = intent.getStringExtra("type") ?: "brandweer"
                findNearest(type)
            }
            "nl.itlusions.atak.calamiteiten.SYNC_NOW" -> {
                // Manual sync
                performManualSync()
            }
        }
    }

    private fun showMainMenu() {
        // TODO: Implement main menu dialog
        Log.d("CalamiteitenMenu", "Showing main menu")
    }

    private fun findNearest(type: String) {
        // TODO: Implement nearest search
        Log.d("CalamiteitenMenu", "Finding nearest $type")
    }

    private fun performManualSync() {
        // TODO: Implement manual sync
        Log.d("CalamiteitenMenu", "Performing manual sync")
    }
}

package nl.itlusions.atak.calamiteiten.map

import android.graphics.Color
import com.atakmap.android.maps.MapView
import com.atakmap.android.maps.Marker
import com.atakmap.android.maps.MapGroup
import com.atakmap.android.maps.PointMapItem
import com.atakmap.coremap.maps.coords.GeoPoint
import nl.itlusions.atak.calamiteiten.data.CalamiteitenRepository
import nl.itlusions.atak.calamiteiten.data.BrandweerKazerne
import kotlinx.coroutines.*

/**
 * Map layer voor brandweerkazernes
 * 
 * Toont alle brandweerkazernes op de ATAK kaart met:
 * - Rode marker voor elke kazerne
 * - 5km coverage zone (cirkel)
 * - Info bij tap: naam, adres, telefoonnummer, type
 */
class BrandweerLayer(private val mapView: MapView) {
    
    private val mapGroup: MapGroup = mapView.rootGroup.addGroup("Brandweer")
    private val markers = mutableMapOf<String, Marker>()
    private val repository = CalamiteitenRepository.getInstance()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    companion object {
        private const val MARKER_TYPE = "b-m-p-s-m" // Emergency medical point
        private const val COVERAGE_RADIUS_METERS = 5000.0 // 5km
        private const val MARKER_ICON = "34ae1613-9645-4222-a9d2-e5f243dea2c0" // Fire icon UUID
    }
    
    init {
        mapGroup.setMetaBoolean("addToObjList", false)
        mapGroup.setMetaBoolean("permaGroup", true)
    }
    
    /**
     * Laad alle brandweerkazernes en toon op kaart
     */
    fun loadData() {
        scope.launch {
            try {
                val kazernes = withContext(Dispatchers.IO) {
                    repository.getBrandweerKazernes()
                }
                
                withContext(Dispatchers.Main) {
                    updateMarkers(kazernes)
                }
            } catch (e: Exception) {
                android.util.Log.e("BrandweerLayer", "Failed to load brandweer data", e)
            }
        }
    }
    
    /**
     * Update markers op kaart
     */
    private fun updateMarkers(kazernes: List<BrandweerKazerne>) {
        // Verwijder oude markers die niet meer bestaan
        val currentIds = kazernes.map { it.id }.toSet()
        markers.keys.toList().forEach { id ->
            if (id !in currentIds) {
                markers[id]?.let { marker ->
                    mapGroup.removeItem(marker)
                }
                markers.remove(id)
            }
        }
        
        // Voeg nieuwe markers toe of update bestaande
        kazernes.forEach { kazerne ->
            val marker = markers[kazerne.id] ?: createMarker(kazerne)
            updateMarker(marker, kazerne)
            markers[kazerne.id] = marker
        }
    }
    
    /**
     * Maak nieuwe marker voor kazerne
     */
    private fun createMarker(kazerne: BrandweerKazerne): Marker {
        val geoPoint = GeoPoint(kazerne.lat, kazerne.lon)
        val marker = PointMapItem.createOrUpdateOGMarker(
            "brandweer_${kazerne.id}",
            geoPoint,
            MARKER_TYPE
        )
        
        marker.setMetaString("menu", "menus/brandweer_menu.xml")
        marker.setMetaInteger("color", Color.RED)
        marker.setMetaString("iconUri", MARKER_ICON)
        marker.setMetaBoolean("addToObjList", true)
        marker.setMetaBoolean("archive", true)
        
        // Voeg coverage zone toe (5km cirkel)
        marker.setMetaDouble("coverageRadius", COVERAGE_RADIUS_METERS)
        marker.setMetaBoolean("showCoverage", false) // Uit standaard, aan via menu
        
        mapGroup.addItem(marker)
        return marker
    }
    
    /**
     * Update marker met kazerne gegevens
     */
    private fun updateMarker(marker: Marker, kazerne: BrandweerKazerne) {
        marker.title = kazerne.naam
        marker.point = GeoPoint(kazerne.lat, kazerne.lon)
        
        // Metadata voor info venster
        marker.setMetaString("callsign", kazerne.naam)
        marker.setMetaString("adres", kazerne.adres)
        marker.setMetaString("plaats", kazerne.plaats)
        marker.setMetaString("postcode", kazerne.postcode)
        marker.setMetaString("telefoon", kazerne.telefoon)
        marker.setMetaString("type", kazerne.type)
        marker.setMetaString("regio", kazerne.veiligheidsregio)
        
        // Extra info
        val remarks = buildString {
            append("Type: ${kazerne.type}\n")
            append("Veiligheidsregio: ${kazerne.veiligheidsregio}\n")
            append("Telefoon: ${kazerne.telefoon}\n")
            append("Adres: ${kazerne.adres}, ${kazerne.postcode} ${kazerne.plaats}")
        }
        marker.setMetaString("remarks", remarks)
        
        // Contact info voor communicatie
        marker.setMetaString("contact_phone", kazerne.telefoon)
        marker.setMetaString("contact_type", "brandweer")
    }
    
    /**
     * Toon/verberg laag
     */
    fun setVisible(visible: Boolean) {
        mapGroup.setVisible(visible)
    }
    
    /**
     * Check of laag zichtbaar is
     */
    fun isVisible(): Boolean = mapGroup.visible
    
    /**
     * Toon coverage zones voor alle kazernes
     */
    fun showCoverageZones(show: Boolean) {
        markers.values.forEach { marker ->
            marker.setMetaBoolean("showCoverage", show)
        }
        mapView.invalidate()
    }
    
    /**
     * Zoek dichtstbijzijnde kazerne bij coördinaat
     */
    fun findNearest(point: GeoPoint): Pair<BrandweerKazerne, Double>? {
        val kazernes = scope.async(Dispatchers.IO) {
            repository.getBrandweerKazernes()
        }
        
        return runBlocking {
            kazernes.await()
                .map { kazerne ->
                    val distance = point.distanceTo(GeoPoint(kazerne.lat, kazerne.lon))
                    Pair(kazerne, distance)
                }
                .minByOrNull { it.second }
        }
    }
    
    /**
     * Cleanup resources
     */
    fun dispose() {
        scope.cancel()
        markers.clear()
        mapGroup.clearGroups()
        mapGroup.clearItems()
    }
    
    /**
     * Refresh data from server
     */
    fun refresh() {
        loadData()
    }
}

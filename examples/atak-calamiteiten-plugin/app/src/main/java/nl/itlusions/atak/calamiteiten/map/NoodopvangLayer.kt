package nl.itlusions.atak.calamiteiten.map

import android.graphics.Color
import com.atakmap.android.maps.MapView
import com.atakmap.android.maps.Marker
import com.atakmap.android.maps.MapGroup
import com.atakmap.android.maps.PointMapItem
import com.atakmap.coremap.maps.coords.GeoPoint
import nl.itlusions.atak.calamiteiten.data.CalamiteitenRepository
import nl.itlusions.atak.calamiteiten.data.NoodopvangLocatie
import kotlinx.coroutines.*

/**
 * Map layer voor noodopvanglocaties
 * 
 * Toont alle noodopvanglocaties op de ATAK kaart met:
 * - Groene marker voor elke locatie
 * - Capaciteit indicator
 * - Faciliteiten iconen (eten, water, medisch, slaapplaatsen)
 * - Info bij tap: naam, adres, capaciteit, beschikbare faciliteiten
 */
class NoodopvangLayer(private val mapView: MapView) {
    
    private val mapGroup: MapGroup = mapView.rootGroup.addGroup("Noodopvang")
    private val markers = mutableMapOf<String, Marker>()
    private val repository = CalamiteitenRepository.getInstance()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    companion object {
        private const val MARKER_TYPE = "b-m-p-c" // Civilian point
        private const val MARKER_ICON = "f7f71666-8b28-4b57-9fbb-e38e61d33b79" // Shelter icon UUID
    }
    
    init {
        mapGroup.setMetaBoolean("addToObjList", false)
        mapGroup.setMetaBoolean("permaGroup", true)
    }
    
    /**
     * Laad alle noodopvanglocaties en toon op kaart
     */
    fun loadData() {
        scope.launch {
            try {
                val locaties = withContext(Dispatchers.IO) {
                    repository.getNoodopvangLocaties()
                }
                
                withContext(Dispatchers.Main) {
                    updateMarkers(locaties)
                }
            } catch (e: Exception) {
                android.util.Log.e("NoodopvangLayer", "Failed to load noodopvang data", e)
            }
        }
    }
    
    /**
     * Update markers op kaart
     */
    private fun updateMarkers(locaties: List<NoodopvangLocatie>) {
        // Verwijder oude markers die niet meer bestaan
        val currentIds = locaties.map { it.id }.toSet()
        markers.keys.toList().forEach { id ->
            if (id !in currentIds) {
                markers[id]?.let { marker ->
                    mapGroup.removeItem(marker)
                }
                markers.remove(id)
            }
        }
        
        // Voeg nieuwe markers toe of update bestaande
        locaties.forEach { locatie ->
            val marker = markers[locatie.id] ?: createMarker(locatie)
            updateMarker(marker, locatie)
            markers[locatie.id] = marker
        }
    }
    
    /**
     * Maak nieuwe marker voor noodopvanglocatie
     */
    private fun createMarker(locatie: NoodopvangLocatie): Marker {
        val geoPoint = GeoPoint(locatie.lat, locatie.lon)
        val marker = PointMapItem.createOrUpdateOGMarker(
            "noodopvang_${locatie.id}",
            geoPoint,
            MARKER_TYPE
        )
        
        marker.setMetaString("menu", "menus/noodopvang_menu.xml")
        marker.setMetaInteger("color", getMarkerColor(locatie))
        marker.setMetaString("iconUri", MARKER_ICON)
        marker.setMetaBoolean("addToObjList", true)
        marker.setMetaBoolean("archive", true)
        
        mapGroup.addItem(marker)
        return marker
    }
    
    /**
     * Bepaal marker kleur op basis van beschikbaarheid
     */
    private fun getMarkerColor(locatie: NoodopvangLocatie): Int {
        return if (locatie.beschikbaar) {
            Color.GREEN  // Beschikbaar
        } else {
            Color.YELLOW // Niet beschikbaar of vol
        }
    }
    
    /**
     * Update marker met locatie gegevens
     */
    private fun updateMarker(marker: Marker, locatie: NoodopvangLocatie) {
        marker.title = locatie.naam
        marker.point = GeoPoint(locatie.lat, locatie.lon)
        
        // Update kleur op basis van beschikbaarheid
        marker.setMetaInteger("color", getMarkerColor(locatie))
        
        // Metadata voor info venster
        marker.setMetaString("callsign", locatie.naam)
        marker.setMetaString("adres", locatie.adres)
        marker.setMetaString("plaats", locatie.plaats)
        marker.setMetaString("postcode", locatie.postcode)
        marker.setMetaInteger("capaciteit", locatie.capaciteit)
        marker.setMetaBoolean("beschikbaar", locatie.beschikbaar)
        marker.setMetaString("type", locatie.type)
        marker.setMetaString("regio", locatie.veiligheidsregio)
        
        // Faciliteiten
        marker.setMetaBoolean("heeft_eten", locatie.heeftEten)
        marker.setMetaBoolean("heeft_water", locatie.heeftWater)
        marker.setMetaBoolean("heeft_medisch", locatie.heeftMedisch)
        marker.setMetaBoolean("heeft_slaapplaatsen", locatie.heeftSlaapplaatsen)
        
        // Extra info voor remarks
        val remarks = buildFaciliteiten(locatie)
        marker.setMetaString("remarks", remarks)
        
        // Contact info
        if (locatie.telefoon.isNotEmpty()) {
            marker.setMetaString("contact_phone", locatie.telefoon)
        }
        marker.setMetaString("contact_type", "noodopvang")
    }
    
    /**
     * Bouw faciliteiten beschrijving
     */
    private fun buildFaciliteiten(locatie: NoodopvangLocatie): String {
        return buildString {
            append("Type: ${locatie.type}\n")
            append("Capaciteit: ${locatie.capaciteit} personen\n")
            append("Status: ${if (locatie.beschikbaar) "Beschikbaar" else "Niet beschikbaar"}\n\n")
            
            append("Faciliteiten:\n")
            if (locatie.heeftEten) append("✓ Eten en drinken\n")
            if (locatie.heeftWater) append("✓ Drinkwater\n")
            if (locatie.heeftMedisch) append("✓ Medische hulp\n")
            if (locatie.heeftSlaapplaatsen) append("✓ Slaapplaatsen\n")
            
            if (!locatie.heeftEten && !locatie.heeftWater && !locatie.heeftMedisch && !locatie.heeftSlaapplaatsen) {
                append("Geen specifieke faciliteiten vermeld\n")
            }
            
            append("\nVeiligheidsregio: ${locatie.veiligheidsregio}\n")
            if (locatie.telefoon.isNotEmpty()) {
                append("Telefoon: ${locatie.telefoon}\n")
            }
            append("Adres: ${locatie.adres}, ${locatie.postcode} ${locatie.plaats}")
        }
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
     * Filter op beschikbaarheid
     */
    fun filterByAvailability(onlyAvailable: Boolean) {
        scope.launch {
            val locaties = withContext(Dispatchers.IO) {
                repository.getNoodopvangLocaties()
            }
            
            withContext(Dispatchers.Main) {
                val filtered = if (onlyAvailable) {
                    locaties.filter { it.beschikbaar }
                } else {
                    locaties
                }
                updateMarkers(filtered)
            }
        }
    }
    
    /**
     * Filter op faciliteiten
     */
    fun filterByFacilities(
        needsFood: Boolean = false,
        needsWater: Boolean = false,
        needsMedical: Boolean = false,
        needsSleeping: Boolean = false
    ) {
        scope.launch {
            val locaties = withContext(Dispatchers.IO) {
                repository.getNoodopvangLocaties()
            }
            
            withContext(Dispatchers.Main) {
                val filtered = locaties.filter { locatie ->
                    (!needsFood || locatie.heeftEten) &&
                    (!needsWater || locatie.heeftWater) &&
                    (!needsMedical || locatie.heeftMedisch) &&
                    (!needsSleeping || locatie.heeftSlaapplaatsen)
                }
                updateMarkers(filtered)
            }
        }
    }
    
    /**
     * Zoek dichtstbijzijnde beschikbare opvang bij coördinaat
     */
    fun findNearest(point: GeoPoint, onlyAvailable: Boolean = true): Pair<NoodopvangLocatie, Double>? {
        val locaties = scope.async(Dispatchers.IO) {
            repository.getNoodopvangLocaties()
        }
        
        return runBlocking {
            locaties.await()
                .filter { !onlyAvailable || it.beschikbaar }
                .map { locatie ->
                    val distance = point.distanceTo(GeoPoint(locatie.lat, locatie.lon))
                    Pair(locatie, distance)
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

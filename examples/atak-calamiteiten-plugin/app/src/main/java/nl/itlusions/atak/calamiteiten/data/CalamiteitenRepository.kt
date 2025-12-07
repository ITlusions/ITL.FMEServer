package nl.itlusions.atak.calamiteiten.data

import android.content.Context
import com.atakmap.coremap.log.Log

/**
 * Main data repository for Calamiteiten plugin
 * 
 * Handles:
 * - API calls to FME Server backend
 * - Local caching (SQLite)
 * - Offline mode
 * - Data synchronization
 */
class CalamiteitenRepository private constructor(
    private val context: Context
) {

    companion object {
        private const val TAG = "CalamiteitenRepo"
        
        @Volatile
        private var INSTANCE: CalamiteitenRepository? = null

        fun getInstance(context: Context): CalamiteitenRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CalamiteitenRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    // API endpoint from settings
    private val prefs = context.getSharedPreferences("calamiteiten_settings", Context.MODE_PRIVATE)
    private val apiEndpoint: String
        get() = prefs.getString("api_endpoint", "") ?: ""
    private val apiKey: String
        get() = prefs.getString("api_key", "") ?: ""

    /**
     * Get all brandweerkazernes
     * Returns cached data if offline
     */
    suspend fun getBrandweerKazernes(): Result<List<BrandweerKazerne>> {
        return try {
            // TODO: Implement API call + caching
            Log.d(TAG, "Getting brandweerkazernes from API: $apiEndpoint")
            
            // Mock data for now
            Result.success(listOf(
                BrandweerKazerne(
                    id = 1,
                    naam = "Brandweer Amsterdam Centrum",
                    plaats = "Amsterdam",
                    latitude = 52.3792,
                    longitude = 4.9003,
                    typeKazerne = "Beroeps",
                    aantalVoertuigen = 8,
                    bemanning247 = true,
                    specialisaties = listOf("Hoogwerker", "Duikteam"),
                    telefoonnummer = "020-5556666",
                    opkomsttijdMinuten = 5
                )
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting brandweerkazernes", e)
            Result.failure(e)
        }
    }

    /**
     * Get all noodopvang locaties
     * Returns cached data if offline
     */
    suspend fun getNoodopvangLocaties(): Result<List<NoodopvangLocatie>> {
        return try {
            Log.d(TAG, "Getting noodopvang locaties from API: $apiEndpoint")
            
            // Mock data for now
            Result.success(listOf(
                NoodopvangLocatie(
                    id = 1,
                    naam = "Sporthal De Eendracht",
                    plaats = "Amsterdam",
                    latitude = 52.3700,
                    longitude = 4.8900,
                    typeLocatie = "Sporthal",
                    capaciteit = 500,
                    capaciteitBeschikbaar = 500,
                    heeftKeuken = true,
                    heeftSanitair = true,
                    heeftNoodstroom = true,
                    heeftEHBO = true,
                    status = "beschikbaar",
                    telefoonnummer = "020-1234567",
                    contactpersoon = "J. de Vries"
                )
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting noodopvang locaties", e)
            Result.failure(e)
        }
    }

    /**
     * Find nearest resources (brandweer or noodopvang)
     */
    suspend fun findNearest(
        lat: Double,
        lon: Double,
        type: String,
        limit: Int = 5
    ): Result<List<NearestResource>> {
        return try {
            Log.d(TAG, "Finding nearest $type at ($lat, $lon)")
            
            // TODO: Implement API call
            Result.success(emptyList())
        } catch (e: Exception) {
            Log.e(TAG, "Error finding nearest resources", e)
            Result.failure(e)
        }
    }

    /**
     * Perform data sync with backend
     */
    suspend fun syncData(): Result<SyncResult> {
        return try {
            Log.i(TAG, "Starting data sync...")
            
            // Sync brandweer
            val brandweerResult = getBrandweerKazernes()
            
            // Sync noodopvang
            val noodopvangResult = getNoodopvangLocaties()
            
            val syncResult = SyncResult(
                success = brandweerResult.isSuccess && noodopvangResult.isSuccess,
                brandweerCount = brandweerResult.getOrNull()?.size ?: 0,
                noodopvangCount = noodopvangResult.getOrNull()?.size ?: 0,
                timestamp = System.currentTimeMillis()
            )
            
            Log.i(TAG, "Sync complete: $syncResult")
            Result.success(syncResult)
            
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            Result.failure(e)
        }
    }
}

/**
 * Brandweerkazerne data model
 */
data class BrandweerKazerne(
    val id: Int,
    val naam: String,
    val plaats: String,
    val latitude: Double,
    val longitude: Double,
    val typeKazerne: String, // "Beroeps", "Vrijwillig", "Gemengd"
    val aantalVoertuigen: Int,
    val bemanning247: Boolean,
    val specialisaties: List<String>,
    val telefoonnummer: String?,
    val opkomsttijdMinuten: Int
)

/**
 * Noodopvanglocatie data model
 */
data class NoodopvangLocatie(
    val id: Int,
    val naam: String,
    val plaats: String,
    val latitude: Double,
    val longitude: Double,
    val typeLocatie: String, // "Sporthal", "School", "Gemeentehuis", etc.
    val capaciteit: Int,
    val capaciteitBeschikbaar: Int,
    val heeftKeuken: Boolean,
    val heeftSanitair: Boolean,
    val heeftNoodstroom: Boolean,
    val heeftEHBO: Boolean,
    val status: String, // "beschikbaar", "vol", "onbeschikbaar"
    val telefoonnummer: String?,
    val contactpersoon: String?
)

/**
 * Nearest resource result
 */
data class NearestResource(
    val id: Int,
    val naam: String,
    val type: String,
    val distanceMeters: Int,
    val etaMinutes: Int?,
    val latitude: Double,
    val longitude: Double
)

/**
 * Sync result
 */
data class SyncResult(
    val success: Boolean,
    val brandweerCount: Int,
    val noodopvangCount: Int,
    val timestamp: Long
)

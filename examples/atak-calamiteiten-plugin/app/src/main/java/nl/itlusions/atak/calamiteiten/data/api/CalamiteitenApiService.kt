package nl.itlusions.atak.calamiteiten.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header

/**
 * Retrofit API interface for Calamiteiten backend
 * 
 * Backend: FME Server with PostGIS database
 * Base URL: https://fme-2025-2.itlusions.nl/api/calamiteiten
 */
interface CalamiteitenApiService {

    /**
     * Get all brandweerkazernes as GeoJSON FeatureCollection
     */
    @GET("brandweer")
    suspend fun getBrandweerKazernes(
        @Header("Authorization") apiKey: String
    ): Response<GeoJsonFeatureCollection>

    /**
     * Get all noodopvang locaties as GeoJSON FeatureCollection
     */
    @GET("noodopvang")
    suspend fun getNoodopvangLocaties(
        @Header("Authorization") apiKey: String
    ): Response<GeoJsonFeatureCollection>

    /**
     * Find nearest resources (brandweer or noodopvang)
     * 
     * @param lat Latitude (WGS84)
     * @param lon Longitude (WGS84)
     * @param type Resource type: "brandweer" or "noodopvang"
     * @param limit Max results (default 5)
     */
    @GET("nearest")
    suspend fun getNearestResources(
        @Header("Authorization") apiKey: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("type") type: String,
        @Query("limit") limit: Int = 5
    ): Response<NearestResourcesResponse>

    /**
     * Get veiligheidsregio's (safety regions)
     */
    @GET("veiligheidsregios")
    suspend fun getVeiligheidsRegios(
        @Header("Authorization") apiKey: String
    ): Response<GeoJsonFeatureCollection>

    /**
     * Health check
     */
    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>
}

/**
 * GeoJSON FeatureCollection response
 */
data class GeoJsonFeatureCollection(
    val type: String = "FeatureCollection",
    val features: List<GeoJsonFeature>
)

/**
 * GeoJSON Feature
 */
data class GeoJsonFeature(
    val type: String = "Feature",
    val id: Int,
    val geometry: GeoJsonGeometry,
    val properties: Map<String, Any?>
)

/**
 * GeoJSON Geometry (Point)
 */
data class GeoJsonGeometry(
    val type: String,
    val coordinates: List<Double> // [longitude, latitude]
)

/**
 * Nearest resources response
 */
data class NearestResourcesResponse(
    val results: List<NearestResource>
)

/**
 * Single nearest resource
 */
data class NearestResource(
    val id: Int,
    val naam: String,
    val type: String,
    val distance_meters: Int,
    val eta_minutes: Int? = null,
    val capaciteit: Int? = null,
    val coordinates: List<Double>
)

/**
 * Health check response
 */
data class HealthResponse(
    val status: String,
    val version: String,
    val timestamp: String
)

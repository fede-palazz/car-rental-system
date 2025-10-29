package com.rentalcarsystem.trackingservice.jobs

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rentalcarsystem.trackingservice.models.TrackingPoint
import com.rentalcarsystem.trackingservice.models.TrackingSession
import com.rentalcarsystem.trackingservice.repositories.TrackingPointRepository
import com.rentalcarsystem.trackingservice.repositories.TrackingSessionRepository
import com.rentalcarsystem.trackingservice.services.TrackingServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClient
import java.time.Instant
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Service
class TrackingJob(
    private val trackingSessionRepository: TrackingSessionRepository,
    private val trackingPointRepository: TrackingPointRepository,
    private val osrmServiceRestClient: RestClient
) {
    private val logger = LoggerFactory.getLogger(TrackingServiceImpl::class.java)
    val turinCenterLat = 45.058360
    val turinCenterLng = 7.665896

    @Transactional
    @Scheduled(fixedDelayString = "\${tracking.generator.interval-ms:2000}")
    fun generateTrackingPoints() {
        trackingSessionRepository.findOngoingSessions().forEach { session ->
            val lastPoint = trackingPointRepository.findTopByTrackingSessionIdOrderByTimestampDesc(session.getId()!!)
            val newPoint = generateNewPointForSession(lastPoint)
            session.addTrackingPoint(newPoint)
            trackingSessionRepository.saveAndFlush(session)
        }
    }

    private fun generateNewPointForSession(lastPoint: TrackingPoint?): TrackingPoint {
        //var bearingDegrees = 0.0 //Random.nextDouble(90.0, 95.0)
        val distanceMeters = Random.nextDouble(30.0, 80.0)
        var bearingDegrees = normalizeBearing(
            (lastPoint?.bearing ?: Random.nextDouble(0.0, 360.0)) +
                    Random.nextDouble(-45.0, 45.0)
        )

        // Calculate next pair of coordinates
        val (lat, lng) = if (lastPoint != null) {
            getNextCoordinate(lastPoint.lat, lastPoint.lng, distanceMeters, bearingDegrees)
        } else {
            turinCenterLat to turinCenterLng    // Default point
        }

        // Place generated coordinates on the actual street
        val (alignedLat, alignedLng) = alignToNearestRoad(lat, lng, bearingDegrees)
        val distanceIncremental = distanceMeters + (lastPoint?.distanceIncremental ?: 0.0)

        if (alignedLat == lastPoint?.lat || alignedLng == lastPoint?.lng) {
            // Change direction
            bearingDegrees = if (lastPoint.bearing == 0.0) 180.0 else 0.0
        }

        return TrackingPoint(
            lat = alignedLat,
            lng = alignedLng,
//            lat = lat,
//            lng = lng,
            timestamp = Instant.now(),
            bearing = bearingDegrees,
            distanceIncremental = distanceIncremental
        )
    }

    /**
     * Generate a new coordinate from a starting point given a distance and optional bearing.
     *
     * @param lat Starting latitude in degrees
     * @param lng Starting longitude in degrees
     * @param distanceMeters Distance to move in meters
     * @param bearingDegrees Bearing in degrees (0 = north, clockwise), default = random
     * @return Pair of (lat, lng) in degrees
     */
    private fun getNextCoordinate(
        lat: Double,
        lng: Double,
        distanceMeters: Double = 100.0,
        bearingDegrees: Double = 0.0,
    ): Pair<Double, Double> {
        // Earth radius in meters
        val earthRadius = 6_371_000.0

        // Convert degrees to radians
        val latRad = Math.toRadians(lat)
        val lngRad = Math.toRadians(lng)
        val bearingRad = Math.toRadians(bearingDegrees)

        // Angular distance
        val delta = distanceMeters / earthRadius

        // New latitude
        val newLatRad = asin(
            sin(latRad) * cos(delta) + cos(latRad) * sin(delta) * cos(bearingRad)
        )

        // New longitude
        val newLngRad = lngRad + atan2(
            sin(bearingRad) * sin(delta) * cos(latRad),
            cos(delta) - sin(latRad) * sin(newLatRad)
        )

        // Convert back to degrees
        val newLat = Math.toDegrees(newLatRad)
        val newLng = Math.toDegrees(newLngRad)

        return newLat to newLng
    }

    private fun alignToNearestRoad(lat: Double, lng: Double, bearing: Double? = null): Pair<Double, Double> {
        val endpoint = StringBuilder("/nearest/v1/driving/$lng,$lat")

        bearing?.let {
            endpoint.append("?bearings=${it.toInt()},45") // 10° tolerance range
        }

        val response = osrmServiceRestClient
            .get()
            .uri(endpoint.toString())
            .retrieve()
            .body(OsrmNearestResponse::class.java)

        return if (response?.code == "Ok" && response.waypoints.isNotEmpty()) {
            val loc = response.waypoints.first().location
            Pair(loc[1], loc[0]) // [lat, lng]
        } else {
            Pair(lat, lng) // fallback to original
        }
    }

    /**
     * Keeps the bearing angle within [0, 359.99] degrees.
     */
    private fun normalizeBearing(bearing: Double): Double {
        var b = bearing % 360.0
        if (b < 0) b += 360.0
        return b
    }

}

data class OsrmNearestResponse(
    val code: String,
    val waypoints: List<Waypoint>
)

data class Waypoint(
    val location: List<Double>, // [lng, lat]
    val name: String?,
    val distance: Double
)
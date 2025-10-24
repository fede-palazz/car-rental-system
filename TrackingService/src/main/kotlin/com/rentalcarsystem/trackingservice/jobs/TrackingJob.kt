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
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClient
import java.time.Instant
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class TrackingJob(
    private val trackingSessionRepository: TrackingSessionRepository,
    private val trackingPointRepository: TrackingPointRepository,
    private val osrmServiceRestClient: RestClient
) {
    private val logger = LoggerFactory.getLogger(TrackingServiceImpl::class.java)
    private val objectMapper = ObjectMapper().registerKotlinModule()

    // Example configuration: every 2 seconds
    @Transactional
    @Scheduled(fixedDelayString = "\${tracking.generator.interval-ms:2000}")
    fun generateTrackingPoints() {
        val ongoingSessions = trackingSessionRepository.findOngoingSessions()
        logger.info("Ongoing sessions: {}", objectMapper.writeValueAsString(ongoingSessions))

        ongoingSessions.forEach { session ->
            val newPoint = generateNewPointForSession(session)
            logger.info("Point for session {}: {}", session.getId() ,objectMapper.writeValueAsString(newPoint))
            //session.addTrackingPoint(newPoint)
            //trackingPointRepository.save(newPoint)
        }
    }

    private fun generateNewPointForSession(session: TrackingSession): TrackingPoint {
        val lastPoint = session.trackingPoints.maxByOrNull { it.timestamp }

        // Call external OSRM or simulate a point
        val (lat, lng) = if (lastPoint != null) {
            // Example call — you can replace this with an OSRM API request
            val newLat = lastPoint.lat + Random.nextDouble(-0.0005, 0.0005)
            val newLng = lastPoint.lng + Random.nextDouble(-0.0005, 0.0005)
            newLat to newLng
        } else {
            // First point (you could get this from OSRM or a known location)
            45.4642 to 9.19 // e.g. Milan center
        }

        return TrackingPoint(
            lat = lat,
            lng = lng,
            timestamp = Instant.now(),
            bearing = Random.nextDouble(0.0, 360.0),
            angle = Random.nextDouble(0.0, 180.0),
            distanceIncremental = Random.nextDouble(0.0, 20.0),
            trackingSession = session
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
        distanceMeters: Double = 150.0,
        bearingDegrees: Double = Random.nextDouble(0.0, 360.0)
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
}
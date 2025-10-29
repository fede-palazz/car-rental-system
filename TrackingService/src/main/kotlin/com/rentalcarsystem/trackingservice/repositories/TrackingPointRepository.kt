package com.rentalcarsystem.trackingservice.repositories

import com.rentalcarsystem.trackingservice.models.TrackingPoint
import com.rentalcarsystem.trackingservice.models.VehicleDailyDistance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
interface TrackingPointRepository: JpaRepository<TrackingPoint, Long> {
    fun findTopByTrackingSessionIdOrderByTimestampDesc(trackingSessionId: Long): TrackingPoint?

    @Query(
        value = """
            SELECT 
                ts.vehicle_id AS vehicleId,
                ((MAX(tp.distance_incremental) - MIN(tp.distance_incremental)) / 1000.0) AS dailyDistanceKm
            FROM tracking_sessions ts INNER JOIN tracking_points tp 
                ON ts.id = tp.tracking_session_id
            WHERE tp.timestamp >= :start
              AND tp.timestamp < :end
            GROUP BY ts.vehicle_id
            ORDER BY ts.vehicle_id
        """,
        nativeQuery = true
    )
    fun findDailyDistanceForDate(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<VehicleDailyDistance>
}
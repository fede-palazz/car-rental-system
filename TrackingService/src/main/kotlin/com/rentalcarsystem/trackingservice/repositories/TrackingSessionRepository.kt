package com.rentalcarsystem.trackingservice.repositories

import com.rentalcarsystem.trackingservice.models.TrackingSession
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TrackingSessionRepository: JpaRepository<TrackingSession, Long> {

    @Query("SELECT ts FROM TrackingSession ts WHERE ts.endDate IS NULL")
    fun findOngoingSessions(): List<TrackingSession>

    @Query("""
    SELECT ts FROM TrackingSession ts WHERE ts.endDate IS NULL AND ts.customerUsername = :customerUsername""")
    fun findOngoingSessionByUsername(@Param("customerUsername") customerUsername: String): TrackingSession?

    @Query("""
    SELECT ts FROM TrackingSession ts WHERE ts.endDate IS NULL AND ts.reservationId = :reservationId""")
    fun findOngoingSessionByReservation(@Param("reservationId") reservationId: Long): TrackingSession?

    @Query("""
    SELECT ts FROM TrackingSession ts WHERE ts.endDate IS NULL AND ts.vehicleId = :vehicleId""")
    fun findOngoingSessionByVehicle(@Param("vehicleId") vehicleId: Long): TrackingSession?

    @Query("""
    SELECT CASE WHEN COUNT(ts) > 0 THEN TRUE ELSE FALSE END
    FROM TrackingSession ts
    WHERE ts.endDate IS NULL AND (
        ts.customerUsername = :customerUsername OR
        ts.vehicleId = :vehicleId OR
        ts.reservationId = :reservationId
    )
    """)
    fun hasConflictingSession(
        @Param("customerUsername") customerUsername: String,
        @Param("reservationId") reservationId: Long,
        @Param("vehicleId") vehicleId: Long
    ): Boolean


}
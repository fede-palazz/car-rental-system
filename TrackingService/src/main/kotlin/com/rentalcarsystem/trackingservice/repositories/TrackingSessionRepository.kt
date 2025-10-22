package com.rentalcarsystem.trackingservice.repositories

import com.rentalcarsystem.trackingservice.models.TrackingSession
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TrackingSessionRepository: JpaRepository<TrackingSession, Long> {

    @Query("SELECT ts FROM TrackingSession ts WHERE ts.endDate IS NULL")
    fun findOngoingSessions(): List<TrackingSession>
}
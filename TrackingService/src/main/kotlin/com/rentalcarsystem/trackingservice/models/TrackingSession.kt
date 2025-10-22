package com.rentalcarsystem.trackingservice.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "tracking_sessions")
class TrackingSession(

    @Column(nullable = false)
    val vehicleId: Long,

    @Column(nullable = false)
    val reservationId: Long,

    @Column(nullable = false)
    val customerUsername: String,

    @Column(nullable = false)
    val startDate: LocalDateTime,

    val endDate: LocalDateTime? = null,

    @OneToMany(mappedBy = "trackingSession", cascade = [CascadeType.ALL], orphanRemoval = true)
    val trackingPoints: MutableList<TrackingPoint> = mutableListOf()

) : BaseEntity<Long>() {

    /**
     * Utility methods to synchronize both sides of the relationships
     * **/

    // Tracking points
    fun addTrackingPoint(point: TrackingPoint) {
        trackingPoints.add(point)
        point.trackingSession = this
    }

    fun removeTrackingPoint(point: TrackingPoint) {
        trackingPoints.remove(point)
        point.trackingSession = null
    }
}
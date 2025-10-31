package com.rentalcarsystem.trackingservice.models

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "tracking_sessions")
class TrackingSession(

    @Column(nullable = false)
    var vehicleId: Long,

    @Column(nullable = false)
    var reservationId: Long,

    @Column(nullable = false)
    var customerUsername: String,

    @Column(nullable = false)
    var startDate: LocalDateTime,

    var endDate: LocalDateTime? = null,

    @JsonIgnore
    @OneToMany(mappedBy = "trackingSession", cascade = [CascadeType.ALL], orphanRemoval = true)
    var trackingPoints: MutableList<TrackingPoint> = mutableListOf()

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
package com.rentalcarsystem.trackingservice.models

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "tracking_points")
class TrackingPoint(

    @Column(nullable = false)
    var lat: Double,

    @Column(nullable = false)
    var lng: Double,

    @Column(nullable = false)
    var timestamp: Instant,

    var bearing: Double? = null,

    var angle: Double? = null,

    var distanceIncremental: Double? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var trackingSession: TrackingSession? = null

) : BaseEntity<Long>()
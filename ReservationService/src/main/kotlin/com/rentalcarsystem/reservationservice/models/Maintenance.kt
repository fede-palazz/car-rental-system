package com.rentalcarsystem.reservationservice.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "maintenances")
class Maintenance(
    @Column(nullable = false)
    var defects: String,

    @Column(nullable = false)
    var completed: Boolean,

    @Column(nullable = true)
    var type: String,

    @Column(nullable = false)
    var upcomingServiceNeeds: String,

    @Column(nullable = false)
    var date: LocalDateTime,

    // A Maintenance Record belongs to one Vehicle only
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var vehicle: Vehicle? = null
) : BaseEntity<Long>()
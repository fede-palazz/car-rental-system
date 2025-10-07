package com.rentalcarsystem.reservationservice.models

import com.rentalcarsystem.reservationservice.enums.MaintenanceType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "maintenances")
class Maintenance(
    @Column(nullable = false)
    var defects: String,

    @Column(nullable = false)
    var completed: Boolean,

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    var type: MaintenanceType,

    @Column(nullable = false)
    var upcomingServiceNeeds: String,

    @Column(nullable = false)
    var startDate: LocalDateTime,

    @Column(nullable = false)
    var plannedEndDate: LocalDateTime,

    var actualEndDate: LocalDateTime? = null,

    @Column(nullable = false)
    var startFleetManagerUsername: String,

    var endFleetManagerUsername: String? = null,

    // A Maintenance Record belongs to one Vehicle only
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var vehicle: Vehicle? = null
) : BaseEntity<Long>()
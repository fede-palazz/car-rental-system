package com.rentalcarsystem.analyticsservice.models

import com.rentalcarsystem.analyticsservice.enums.MaintenanceType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "maintenances")
class Maintenance(
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    var type: MaintenanceType,

    @Column(nullable = false)
    var startDate: LocalDateTime,

    @Column(nullable = false)
    var plannedEndDate: LocalDateTime,

    var actualEndDate: LocalDateTime? = null,

    @Column(nullable = false)
    var startFleetManagerUsername: String,

    var endFleetManagerUsername: String? = null

) : BaseEntity<Long>()
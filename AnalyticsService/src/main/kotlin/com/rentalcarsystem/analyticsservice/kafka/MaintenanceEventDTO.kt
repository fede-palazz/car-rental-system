package com.rentalcarsystem.analyticsservice.kafka

import com.rentalcarsystem.analyticsservice.enums.EventType
import com.rentalcarsystem.analyticsservice.enums.MaintenanceType
import com.rentalcarsystem.analyticsservice.models.Maintenance
import java.time.LocalDateTime

data class MaintenanceEventDTO(
    val type: EventType,
    val maintenance: MaintenanceResDTO,
    val startFleetManagerUsername: String? = null,  // Optional since it is unnecessary for deleted events
    val endFleetManagerUsername: String? = null,  // Optional since it is unnecessary for deleted events
)

data class MaintenanceResDTO(
    val id: Long,
    val defects: String,
    val type: MaintenanceType,
    val upcomingServiceNeeds: String,
    val startDate: LocalDateTime,
    val plannedEndDate: LocalDateTime,
    val actualEndDate: LocalDateTime?
)

fun MaintenanceResDTO.toEntity(startFleetManagerUsername: String) = Maintenance(
    type = this.type,
    startDate = this.startDate,
    plannedEndDate = this.plannedEndDate,
    startFleetManagerUsername = startFleetManagerUsername
)
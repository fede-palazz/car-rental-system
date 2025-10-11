package com.rentalcarsystem.reservationservice.dtos.response

import com.rentalcarsystem.reservationservice.enums.MaintenanceType
import com.rentalcarsystem.reservationservice.models.Maintenance
import java.time.LocalDateTime

data class MaintenanceResDTO(
    val id: Long,
    val defects: String,
    val type: MaintenanceType,
    val upcomingServiceNeeds: String,
    val startDate: LocalDateTime,
    val plannedEndDate: LocalDateTime,
    val actualEndDate: LocalDateTime?
)

fun Maintenance.toResDTO() = MaintenanceResDTO(
    id = this.getId()!!,
    defects = this.defects,
    type = this.type,
    upcomingServiceNeeds = this.upcomingServiceNeeds,
    startDate = this.startDate,
    plannedEndDate = this.plannedEndDate,
    actualEndDate = this.actualEndDate
)
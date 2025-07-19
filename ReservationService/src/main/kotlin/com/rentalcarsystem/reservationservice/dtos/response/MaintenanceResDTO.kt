package com.rentalcarsystem.reservationservice.dtos.response

import com.rentalcarsystem.reservationservice.models.Maintenance
import java.time.LocalDateTime

data class MaintenanceResDTO(
    val id: Long,
    val defects: String,
    val completed: Boolean,
    val type: String,
    val upcomingServiceNeeds: String,
    val date: LocalDateTime,
)

fun Maintenance.toResDTO() = MaintenanceResDTO(
    this.getId()!!,
    defects,
    completed,
    type,
    upcomingServiceNeeds,
    date,
)
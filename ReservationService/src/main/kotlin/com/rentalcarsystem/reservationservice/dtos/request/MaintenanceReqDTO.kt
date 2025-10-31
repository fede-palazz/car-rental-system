package com.rentalcarsystem.reservationservice.dtos.request

import com.rentalcarsystem.reservationservice.enums.MaintenanceType
import com.rentalcarsystem.reservationservice.models.Maintenance
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class MaintenanceReqDTO(
    @field:NotBlank(message = "Defects cannot be blank")
    val defects: String,
    val type: MaintenanceType,
    @field:NotBlank(message = "Upcoming service needs cannot be blank")
    val upcomingServiceNeeds: String,
    @field:Future(message = "Parameter 'startDate' must be a future date")
    val startDate: LocalDateTime,
    @field:Future(message = "Parameter 'plannedEndDate' must be a future date")
    val plannedEndDate: LocalDateTime,
)

fun MaintenanceReqDTO.toEntity(username: String) = Maintenance(
    defects = this.defects,
    type = this.type,
    upcomingServiceNeeds = this.upcomingServiceNeeds,
    startDate = this.startDate,
    plannedEndDate = this.plannedEndDate,
    startFleetManagerUsername = username
)

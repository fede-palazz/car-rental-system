package com.rentalcarsystem.reservationservice.dtos.request

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.rentalcarsystem.reservationservice.enums.MaintenanceType
import com.rentalcarsystem.reservationservice.models.Maintenance
import com.rentalcarsystem.reservationservice.utils.CustomBooleanDeserializer
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class MaintenanceReqDTO(
    @field:NotBlank(message = "Defects cannot be blank")
    val defects: String,
    @field:JsonDeserialize(using = CustomBooleanDeserializer::class)
    val completed: Boolean,
    @field:NotBlank(message = "Type cannot be blank")
    val type: MaintenanceType,
    @field:NotBlank(message = "Upcoming service needs cannot be blank")
    val upcomingServiceNeeds: String,
    @field:NotBlank(message = "Start date cannot be blank")
    val startDate: LocalDateTime,
    @field:NotBlank(message = "Planned end date cannot be blank")
    val plannedEndDate: LocalDateTime,
    val actualEndDate: LocalDateTime?,
)

fun MaintenanceReqDTO.toEntity(username: String) = Maintenance(
    defects = this.defects,
    completed = this.completed,
    type = this.type,
    upcomingServiceNeeds = this.upcomingServiceNeeds,
    startDate = this.startDate,
    plannedEndDate = this.plannedEndDate,
    actualEndDate = this.actualEndDate,
    fleetManagerUsername = username,
)

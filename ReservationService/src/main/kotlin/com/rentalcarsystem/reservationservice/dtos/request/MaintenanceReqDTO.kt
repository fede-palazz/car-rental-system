package com.rentalcarsystem.reservationservice.dtos.request

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
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
    val type: String,
    val upcomingServiceNeeds: String?,
)

fun MaintenanceReqDTO.toEntity() = Maintenance(
    defects = this.defects,
    completed = this.completed,
    type = this.type,
    upcomingServiceNeeds = this.upcomingServiceNeeds ?: "",
    date = LocalDateTime.now(),
)

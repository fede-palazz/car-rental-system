package com.rentalcarsystem.reservationservice.dtos.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import kotlin.Long

data class SessionReqDTO(
    @field:NotNull(message = "Parameter 'vehicleId' is required")
    @field:Positive(message = "Parameter 'vehicleId' is invalid")
    val vehicleId: Long,

    @field:NotNull(message = "Parameter 'reservationId' is required")
    @field:Positive(message = "Parameter 'reservationId' is invalid")
    val reservationId: Long,

    @field:NotBlank(message = "Parameter 'customerUsername' is required")
    val customerUsername: String,
)
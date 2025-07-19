package com.rentalcarsystem.reservationservice.dtos.request.reservation

import jakarta.validation.constraints.Future
import java.time.LocalDateTime

data class ReservationUpdateReqDTO(
    @field:Future(message = "Parameter 'plannedPickupDate' must be a future date")
    val plannedPickUpDate: LocalDateTime?,
    @field:Future(message = "Parameter 'plannedDropOffDate' must be a future date")
    val plannedDropOffDate: LocalDateTime?,
)

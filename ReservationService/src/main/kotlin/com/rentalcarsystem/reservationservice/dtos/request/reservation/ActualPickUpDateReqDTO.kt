package com.rentalcarsystem.reservationservice.dtos.request.reservation

import jakarta.validation.constraints.PastOrPresent
import java.time.LocalDateTime

data class ActualPickUpDateReqDTO(
    @field:PastOrPresent(message = "Parameter 'actualPickUpDate' must be a past or present date")
    val actualPickUpDate: LocalDateTime
)

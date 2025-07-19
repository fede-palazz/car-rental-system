package com.rentalcarsystem.reservationservice.dtos.request.reservation

import jakarta.validation.constraints.FutureOrPresent
import java.time.LocalDateTime

data class ActualPickUpDateReqDTO(
    // @field:PastOrPresent(message = "Parameter 'actualPickUpDate' must be a past or present date") TODO: Uncomment this line after testing
    val actualPickUpDate: LocalDateTime
)

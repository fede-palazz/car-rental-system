package com.rentalcarsystem.analyticsservice.dtos.response

import java.time.LocalDateTime

data class ReservationsTotalAmountResDTO(
    val elementStart: LocalDateTime,
    val reservationsTotalAmount: Double
)

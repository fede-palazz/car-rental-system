package com.rentalcarsystem.analyticsservice.dtos.response

import java.time.LocalDateTime

data class ReservationsCountResDTO(
    val elementStart: LocalDateTime,
    val reservationsCount: Int
)

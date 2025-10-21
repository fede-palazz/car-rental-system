package com.rentalcarsystem.analyticsservice.services

import com.rentalcarsystem.analyticsservice.dtos.response.ReservationsCountResDTO
import com.rentalcarsystem.analyticsservice.dtos.response.ReservationsTotalAmountResDTO
import com.rentalcarsystem.analyticsservice.enums.Granularity
import java.time.LocalDateTime

interface ReservationService {
    fun getReservationsCount(
        desiredStart: LocalDateTime,
        desiredEnd: LocalDateTime,
        granularity: Granularity
    ): List<ReservationsCountResDTO>

    fun getReservationsTotalAmount(
        desiredStart: LocalDateTime,
        desiredEnd: LocalDateTime,
        granularity: Granularity,
        average: Boolean
    ): List<ReservationsTotalAmountResDTO>
}
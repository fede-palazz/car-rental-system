package com.rentalcarsystem.reservationservice.kafka

import com.rentalcarsystem.reservationservice.dtos.response.reservation.StaffReservationResDTO
import com.rentalcarsystem.reservationservice.enums.EventType

data class ReservationEventDTO(
    val type: EventType,
    val reservation: StaffReservationResDTO
)

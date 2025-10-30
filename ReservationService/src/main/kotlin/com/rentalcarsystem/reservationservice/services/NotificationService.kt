package com.rentalcarsystem.reservationservice.services

import com.rentalcarsystem.reservationservice.kafka.ReservationEventDTO

interface NotificationService {

    fun sendReservationConfirmedEmail(
        recipientEmail: String,
        recipientName: String,
        reservationEvent: ReservationEventDTO
    )

    fun sendReservationCancelledEmail(
        recipientEmail: String,
        recipientName: String,
        reservationEvent: ReservationEventDTO
    )
}
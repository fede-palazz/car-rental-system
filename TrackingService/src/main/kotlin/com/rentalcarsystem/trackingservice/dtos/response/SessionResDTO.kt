package com.rentalcarsystem.trackingservice.dtos.response

import com.rentalcarsystem.trackingservice.models.TrackingPoint
import com.rentalcarsystem.trackingservice.models.TrackingSession
import java.time.LocalDateTime

data class SessionResDTO(
    val id: Long,
    val vehicleId: Long,
    val reservationId: Long,
    val customerUsername: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime?,
    val trackingPoints: MutableList<TrackingPoint>
)

fun TrackingSession.toResDTO() = SessionResDTO(
    this.getId()!!,
    vehicleId,
    reservationId,
    customerUsername,
    startDate,
    endDate,
    trackingPoints
)

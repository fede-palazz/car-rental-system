package com.rentalcarsystem.trackingservice.dtos.response

import com.rentalcarsystem.trackingservice.models.TrackingSession
import java.time.temporal.ChronoUnit

data class SessionResDTO(
    val id: Long,
    val vehicleId: Long,
    val reservationId: Long,
    val customerUsername: String,
    val startDate: String,
    val endDate: String?,
    val lastTrackingPoint: PointResDTO?
)

fun TrackingSession.toResDTO(lastTrackingPoint: PointResDTO? = null) = SessionResDTO(
    this.getId()!!,
    vehicleId,
    reservationId,
    customerUsername,
    startDate.truncatedTo(ChronoUnit.SECONDS).toString(),
    endDate?.truncatedTo(ChronoUnit.SECONDS).toString(),
    lastTrackingPoint
)

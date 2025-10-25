package com.rentalcarsystem.trackingservice.dtos.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.rentalcarsystem.trackingservice.models.TrackingPoint
import com.rentalcarsystem.trackingservice.models.TrackingSession
import jakarta.persistence.Column
import java.time.Instant
import java.time.LocalDateTime

data class PointResDTO(
    val id: Long,
    val lat: Double,
    val lng: Double,
    val timestamp: String,
    val bearing: Double? = null,
    val distanceIncremental: Double? = null,
)

fun TrackingPoint.toResDTO() = PointResDTO(
    this.getId()!!,
    lat,
    lng,
    timestamp.toString(),
    bearing,
    distanceIncremental
)

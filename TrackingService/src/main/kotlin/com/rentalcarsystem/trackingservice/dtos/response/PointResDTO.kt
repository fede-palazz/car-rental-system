package com.rentalcarsystem.trackingservice.dtos.response

import com.rentalcarsystem.trackingservice.models.TrackingPoint
import java.time.temporal.ChronoUnit

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
    timestamp.truncatedTo(ChronoUnit.SECONDS).toString(),
    bearing,
    distanceIncremental
)

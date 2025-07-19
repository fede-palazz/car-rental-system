package com.rentalcarsystem.reservationservice.dtos.response

import com.rentalcarsystem.reservationservice.models.CarFeature

data class CarFeatureResDTO(
    val id: Long,
    val description: String,
)

fun CarFeature.toResDTO() = CarFeatureResDTO(
    this.getId()!!,
    description
)
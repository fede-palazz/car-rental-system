package com.rentalcarsystem.analyticsservice.dtos.response

import java.time.LocalDate

data class VehicleKmTravelledResDTO(
    val elementStart: LocalDate,
    val vehicleKmTravelled: Double
)

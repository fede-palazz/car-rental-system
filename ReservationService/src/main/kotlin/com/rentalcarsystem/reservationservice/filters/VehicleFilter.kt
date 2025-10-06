package com.rentalcarsystem.reservationservice.filters

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import com.rentalcarsystem.reservationservice.enums.CarStatus

data class VehicleFilter(
    @field:Size(min = 1, max = 7, message = "Parameter 'licensePlate' must be maximum 7 characters long")
    val licensePlate: String?,
    @field:Size(min = 1, max = 17, message = "Parameter 'vin' must be maximum 17 characters long")
    val vin: String? = null,
    val brand: String? = null,
    val model: String? = null,
    @field:Max(2100, message = "Parameter 'year' must be a valid year")
    @field:Min(1900, message = "Parameter 'year' must be a valid year")
    val year: Int? = null,
    val status: CarStatus? = null,
    @field:PositiveOrZero(message = "Parameter 'minKmTravelled' must be positive")
    val minKmTravelled: Double? = null,
    @field:PositiveOrZero(message = "Parameter 'maxKmTravelled' must be positive")
    val maxKmTravelled: Double? = null,
    val pendingCleaning: Boolean? = null,
    val pendingRepair: Boolean? = null,
)

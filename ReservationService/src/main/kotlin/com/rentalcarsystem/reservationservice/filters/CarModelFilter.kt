package com.rentalcarsystem.reservationservice.filters

import com.rentalcarsystem.reservationservice.enums.CarCategory
import com.rentalcarsystem.reservationservice.enums.CarSegment
import com.rentalcarsystem.reservationservice.enums.Drivetrain
import com.rentalcarsystem.reservationservice.enums.EngineType
import com.rentalcarsystem.reservationservice.enums.TransmissionType
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.PositiveOrZero

data class CarModelFilter(
    val brand: String? = null,
    val model: String? = null,
    @field:Max(2100, message = "Parameter 'year' must be a valid year")
    @field:Min(1900, message = "Parameter 'year' must be a valid year")
    val year: Int? = null,
    val search: String? = null,
    val segment: CarSegment? = null,
    val category: CarCategory? = null,
    val engineType: EngineType? = null,
    val transmissionType: TransmissionType? = null,
    val drivetrain: Drivetrain? = null,
    @field:PositiveOrZero(message = "Parameter 'minRentalPrice' must be positive")
    val minRentalPrice: Double? = null,
    @field:PositiveOrZero(message = "Parameter 'maxRentalPrice' must be positive")
    val maxRentalPrice: Double? = null,
)

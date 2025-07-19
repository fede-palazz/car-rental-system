package com.rentalcarsystem.reservationservice.filters

import com.rentalcarsystem.reservationservice.enums.ReservationStatus
import jakarta.validation.constraints.*
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

data class ReservationFilter(
    @field:Size(min = 1, max = 7, message = "Parameter 'licensePlate' must be maximum 7 characters long")
    val licensePlate: String?,
    @field:Size(min = 1, max = 17, message = "Parameter 'licensePlate' must be maximum 17 characters long")
    val vin: String? = null,
    val brand: String? = null,
    val model: String? = null,
    @field:Max(2100, message = "Parameter 'year' must be a valid year")
    @field:Min(1900, message = "Parameter 'year' must be a valid year")
    val year: Int? = null,
    @field:Past(message = "Parameter 'minCreationDate' must be a past date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val minCreationDate: LocalDateTime? = null,
    @field:Past(message = "Parameter 'maxCreationDate' must be a past date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val maxCreationDate: LocalDateTime? = null,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val minPlannedPickUpDate: LocalDateTime? = null,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val maxPlannedPickUpDate: LocalDateTime? = null,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val minActualPickUpDate: LocalDateTime? = null,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val maxActualPickUpDate: LocalDateTime? = null,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val minPlannedDropOffDate: LocalDateTime? = null,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val maxPlannedDropOffDate: LocalDateTime? = null,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val minActualDropOffDate: LocalDateTime? = null,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val maxActualDropOffDate: LocalDateTime? = null,
    val status: ReservationStatus? = null,
    @field:PositiveOrZero(message = "Parameter 'minTotalAmount' must be positive")
    val minTotalAmount: Double? = null,
    @field:PositiveOrZero(message = "Parameter 'maxTotalAmount' must be positive")
    val maxTotalAmount: Double? = null,
    val wasDeliveryLate: Boolean? = null,
    val wasChargedFee: Boolean? = null,
    val wasVehicleDamaged: Boolean? = null,
    val wasInvolvedInAccident: Boolean? = null,
    val customerUsername: String? = null
)

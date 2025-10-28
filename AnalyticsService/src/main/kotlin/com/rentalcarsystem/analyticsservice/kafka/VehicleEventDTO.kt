package com.rentalcarsystem.analyticsservice.kafka

import com.rentalcarsystem.analyticsservice.enums.CarStatus
import com.rentalcarsystem.analyticsservice.enums.EventType
import com.rentalcarsystem.analyticsservice.models.Vehicle
import java.time.LocalDate

data class VehicleEventDTO(
    val type: EventType,
    val vehicles: List<VehicleResDTO>
)

data class VehicleResDTO(
    val id: Long,
    val licensePlate: String,
    val vin: String,
    val status: CarStatus,
    val kmTravelled: Double,
    val pendingCleaning: Boolean,
    val brand: String,
    val model: String,
    val year: String,
    val carModelId: Long,
)

fun VehicleResDTO.toEntity() = Vehicle(
    entryDate = LocalDate.now(),
    licensePlate = this.licensePlate,
    vin = this.vin,
    status = this.status,
    kmTravelled = this.kmTravelled,
    pendingCleaning = this.pendingCleaning
)

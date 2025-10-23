package com.rentalcarsystem.reservationservice.dtos.response

import com.rentalcarsystem.reservationservice.models.Vehicle
import com.rentalcarsystem.reservationservice.enums.CarStatus

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

fun Vehicle.toResDTO() = VehicleResDTO(
    this.getId()!!,
    this.licensePlate,
    this.vin,
    this.status,
    this.kmTravelled,
    this.pendingCleaning,
    this.carModel.brand,
    this.carModel.model,
    this.carModel.year,
    this.carModel.getId()!!,
)

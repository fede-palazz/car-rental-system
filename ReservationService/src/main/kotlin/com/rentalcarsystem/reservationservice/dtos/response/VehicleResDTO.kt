package com.rentalcarsystem.reservationservice.dtos.response

import com.rentalcarsystem.reservationservice.models.Vehicle

data class VehicleResDTO(
    val id: Long,
    val licensePlate: String,
    val vin: String,
    val kmTravelled: Double,
    val pendingCleaning: Boolean,
    val pendingRepair: Boolean,
    val brand: String,
    val model: String,
    val year: String,
    val carModelId: Long,
)

fun Vehicle.toResDTO() = VehicleResDTO(
    this.getId()!!,
    this.licensePlate,
    this.vin,
    this.kmTravelled,
    this.pendingCleaning,
    this.pendingRepair,
    this.carModel.brand,
    this.carModel.model,
    this.carModel.year,
    this.carModel.getId()!!,
)

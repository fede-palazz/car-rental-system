package com.rentalcarsystem.reservationservice.dtos.request

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.rentalcarsystem.reservationservice.enums.CarStatus
import com.rentalcarsystem.reservationservice.models.CarModel
import com.rentalcarsystem.reservationservice.models.Vehicle
import com.rentalcarsystem.reservationservice.utils.CustomBooleanDeserializer
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size

data class VehicleReqDTO(
    @field:NotBlank(message = "Parameter 'licensePlate' is required")
    @field:Size(min = 7, max = 7, message = "License plate must be 7 characters long")
    val licensePlate: String,

    @field:NotBlank(message = "Parameter 'vin' is required")
    @field:Size(min = 17, max = 17, message = "VIN must be 17 characters long")
    val vin: String,

    val status: CarStatus?,

    @field:PositiveOrZero(message = "Km travelled must be a positive number or zero")
    val kmTravelled: Double?,

    @field:JsonDeserialize(using = CustomBooleanDeserializer::class)
    val pendingCleaning: Boolean?,

    @field:JsonDeserialize(using = CustomBooleanDeserializer::class)
    val pendingRepair: Boolean?,

    @field:Positive(message = "Parameter 'carModelId' must be a positive number")
    val carModelId: Long
)

fun VehicleReqDTO.toEntity(carModel: CarModel) = Vehicle(
    licensePlate = this.licensePlate,
    vin = this.vin,
    status = this.status ?: CarStatus.AVAILABLE,
    kmTravelled = this.kmTravelled ?: 0.0,
    pendingCleaning = this.pendingCleaning ?: false,
    pendingRepair = this.pendingRepair ?: false,
    carModel = carModel
)
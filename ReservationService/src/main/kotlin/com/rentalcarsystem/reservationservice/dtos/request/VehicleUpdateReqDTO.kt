package com.rentalcarsystem.reservationservice.dtos.request

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.rentalcarsystem.reservationservice.enums.CarStatus
import com.rentalcarsystem.reservationservice.utils.CustomBooleanDeserializer
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size

data class VehicleUpdateReqDTO(
    @field:Size(min = 7, max = 7, message = "License plate must be 7 characters long")
    val licensePlate: String?,
    val status: CarStatus,
    @field:PositiveOrZero(message = "Km travelled must be a positive number or zero")
    val kmTravelled: Double,
    @field:JsonDeserialize(using = CustomBooleanDeserializer::class)
    val pendingCleaning: Boolean?
)

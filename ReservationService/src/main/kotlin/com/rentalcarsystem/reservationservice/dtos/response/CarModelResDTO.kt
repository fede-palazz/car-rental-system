package com.rentalcarsystem.reservationservice.dtos.response

import com.rentalcarsystem.reservationservice.enums.CarCategory
import com.rentalcarsystem.reservationservice.enums.CarSegment
import com.rentalcarsystem.reservationservice.enums.Drivetrain
import com.rentalcarsystem.reservationservice.enums.EngineType
import com.rentalcarsystem.reservationservice.enums.TransmissionType
import com.rentalcarsystem.reservationservice.models.CarModel

data class CarModelResDTO(
    val id: Long,
    val brand: String,
    val model: String,
    val year: String,
    val segment: CarSegment,
    val doorsNumber: Int,
    val seatingCapacity: Int,
    val luggageCapacity: Double,
    val category: CarCategory,
    val features: List<CarFeatureResDTO>,
    val engineType: EngineType,
    val transmissionType: TransmissionType,
    val drivetrain: Drivetrain,
    val motorDisplacement: Int?,
    val rentalPrice: Double
)

fun CarModel.toResDTO() = CarModelResDTO(
    this.getId()!!,
    brand,
    model,
    year,
    segment,
    doorsNumber,
    seatingCapacity,
    luggageCapacity,
    category,
    features.map { it.toResDTO() },
    engineType,
    transmissionType,
    drivetrain,
    motorDisplacement,
    rentalPrice
)

package com.rentalcarsystem.analyticsservice.kafka

import com.rentalcarsystem.analyticsservice.enums.CarCategory
import com.rentalcarsystem.analyticsservice.enums.CarSegment
import com.rentalcarsystem.analyticsservice.enums.Drivetrain
import com.rentalcarsystem.analyticsservice.enums.EngineType
import com.rentalcarsystem.analyticsservice.enums.EventType
import com.rentalcarsystem.analyticsservice.enums.TransmissionType
import com.rentalcarsystem.analyticsservice.models.CarModel

data class CarModelEventDTO(
    val type: EventType,
    val carModel: CarModelResDTO? = null,  // Optional since it is unnecessary for deleted events
    val compositeId: String? = null  // Used for updated and deleted events to identify the car model in format: "brand,model,year"
)

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

data class CarFeatureResDTO(
    val id: Long,
    val description: String,
)

fun CarModelResDTO.toEntity() = CarModel(
    brand = this.brand,
    model = this.model,
    year = this.year,
    segment = this.segment,
    category = this.category,
    engineType = this.engineType,
    transmissionType = this.transmissionType,
    drivetrain = this.drivetrain,
    rentalPrice = this.rentalPrice
)

package com.rentalcarsystem.reservationservice.dtos.request

import com.rentalcarsystem.reservationservice.enums.CarCategory
import com.rentalcarsystem.reservationservice.enums.CarSegment
import com.rentalcarsystem.reservationservice.enums.Drivetrain
import com.rentalcarsystem.reservationservice.enums.EngineType
import com.rentalcarsystem.reservationservice.enums.TransmissionType
import com.rentalcarsystem.reservationservice.models.CarFeature
import com.rentalcarsystem.reservationservice.models.CarModel
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class CarModelReqDTO(
    @field:NotBlank(message = "Brand must not be blank")
    val brand: String,

    @field:NotBlank(message = "Model must not be blank")
    val model: String,

    @field:NotBlank(message = "Year must not be blank")
    @field:Size(min = 4, max = 4, message = "Year must be exactly 4 digits")
    val year: String,

    @field:NotNull(message = "Segment must not be blank")
    val segment: CarSegment,

    @field:Positive(message = "Number of doors must be greater than zero")
    val doorsNumber: Int,

    @field:Positive(message = "Seating capacity must be greater than zero")
    val seatingCapacity: Int,

    @field:Positive(message = "Luggage capacity must be greater than zero")
    val luggageCapacity: Double,

    @field:NotNull(message = "Category must not be blank")
    val category: CarCategory,

    val featureIds: List<Long>,

    val engineType: EngineType,

    val transmissionType: TransmissionType,

    val drivetrain: Drivetrain,

    @field:Positive(message = "Motor displacement must be greater than zero if specified")
    val motorDisplacement: Int?,

    @field:Positive(message = "Rental price must be greater than zero")
    val rentalPrice: Double,
)

fun CarModelReqDTO.toEntity(
    features: MutableSet<CarFeature> = mutableSetOf()
) = CarModel(
    brand = this.brand,
    model = this.model,
    year = this.year,
    segment = this.segment,
    doorsNumber = this.doorsNumber,
    seatingCapacity = this.seatingCapacity,
    luggageCapacity = this.luggageCapacity,
    category = this.category,
    features = features,
    engineType = this.engineType,
    transmissionType = this.transmissionType,
    drivetrain = this.drivetrain,
    motorDisplacement = this.motorDisplacement,
    rentalPrice = this.rentalPrice,
)


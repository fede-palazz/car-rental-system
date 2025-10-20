package com.rentalcarsystem.analyticsservice.models

import com.rentalcarsystem.analyticsservice.enums.*
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

@Entity
@Table(
    name = "car_models",
    uniqueConstraints = [UniqueConstraint(columnNames = ["brand", "model", "year"])]
)
class CarModel(
    @Column(nullable = false)
    @field:NotBlank
    var brand: String,

    @Column(nullable = false)
    @field:NotBlank
    var model: String,

    @Column(nullable = false, length = 4)
    @field:Size(min = 4, max = 4)
    var year: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @field:NotNull
    var segment: CarSegment,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @field:NotNull
    var category: CarCategory,

    // Technical specifications
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var engineType: EngineType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var transmissionType: TransmissionType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var drivetrain: Drivetrain,

    @Column(nullable = false)
    @field:Positive
    var rentalPrice: Double,        // Cost per day based on model and category

) : BaseEntity<Long>()
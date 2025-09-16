package com.rentalcarsystem.reservationservice.models

import com.rentalcarsystem.reservationservice.enums.*
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size


@Entity
@Table(
    name = "car_models",
    uniqueConstraints = [UniqueConstraint(columnNames = ["brand", "model"])]
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

    @Column(nullable = false)
    @field:Positive
    var doorsNumber: Int,

    @Column(nullable = false)
    @field:Positive
    var seatingCapacity: Int,

    @Column(nullable = false)
    @field:Positive
    var luggageCapacity: Double,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @field:NotNull
    var category: CarCategory,

    @ManyToMany
    @JoinTable(
        name = "car_models_features",
        joinColumns = [JoinColumn(name = "car_model_id")],
        inverseJoinColumns = [JoinColumn(name = "feature_id")]
    )
    var features: MutableSet<CarFeature> = mutableSetOf(),

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

    @Column(nullable = true)
    @field:Positive
    var motorDisplacement: Int?,      // Null for electric cars

    @Column(nullable = false)
    @field:Positive
    var rentalPrice: Double,        // Cost per day based on model and category

    @Column(name = "search_vector", columnDefinition = "tsvector", insertable = false, updatable = false)
    val searchVector: String? = null,
) : BaseEntity<Long>()


package com.rentalcarsystem.reservationservice.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "car_features")
class CarFeature(
    @Column(nullable = false, unique = true)
    var description: String,
) : BaseEntity<Long>()
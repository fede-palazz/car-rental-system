package com.rentalcarsystem.analyticsservice.models

import com.rentalcarsystem.analyticsservice.enums.CarStatus
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "vehicles",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["entry_date", "license_plate"]),
        UniqueConstraint(columnNames = ["entry_date", "vin"])
    ]
)
class Vehicle(
    @Column(nullable = false, name = "entry_date")
    var entryDate: LocalDate,

    @Column(nullable = false, length = 7, name = "license_plate")
    var licensePlate: String,

    @Column(nullable = false, length = 17, name = "vin")
    var vin: String,    // Vehicle Identification Number

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: CarStatus,

    @Column(nullable = false)
    var kmTravelled: Double,

    @Column(nullable = false)
    var pendingCleaning: Boolean
) : BaseEntity<Long>()
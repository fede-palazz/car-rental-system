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

    // Many Vehicles can refer to the same CarModel
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var carModel: CarModel,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: CarStatus,

    @Column(nullable = false)
    var kmTravelled: Double,

    @Column(nullable = false)
    var pendingCleaning: Boolean,

    // One Vehicle can have many Maintenance Records
    @OneToMany(mappedBy = "vehicle", cascade = [CascadeType.ALL], orphanRemoval = true)
    var maintenances: MutableSet<Maintenance> = mutableSetOf(),

    // One Vehicle can have many Reservations
    @OneToMany(mappedBy = "vehicle", cascade = [CascadeType.ALL], orphanRemoval = true)
    var reservations: MutableSet<Reservation> = mutableSetOf()
) : BaseEntity<Long>() {

    /**
     * Utility methods to synchronize both sides of the relationships
     * **/

    // Maintenances
    fun addMaintenance(maintenance: Maintenance) {
        maintenances.add(maintenance)
        maintenance.vehicle = this
    }

    fun removeMaintenance(maintenance: Maintenance) {
        maintenances.remove(maintenance)
        maintenance.vehicle = null
    }

    // Reservations
    fun addReservation(reservation: Reservation) {
        reservations.add(reservation)
        reservation.vehicle = this
    }

    fun removeReservation(reservation: Reservation) {
        reservations.remove(reservation)
        reservation.vehicle = null
    }
}
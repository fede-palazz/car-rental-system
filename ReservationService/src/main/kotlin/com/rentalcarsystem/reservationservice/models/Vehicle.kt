package com.rentalcarsystem.reservationservice.models

import jakarta.persistence.*

@Entity
@Table(name = "vehicles")
class Vehicle(
    @Column(unique = true, nullable = false, length = 7)
    var licensePlate: String,

    @Column(unique = true, nullable = false, length = 17)
    var vin: String,    // Vehicle Identification Number

    // Many Vehicles can refer to the same CarModel
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var carModel: CarModel,

    @Column(nullable = false)
    var kmTravelled: Double,

    @Column(nullable = false)
    var pendingCleaning: Boolean,

    @Column(nullable = false)
    var pendingRepair: Boolean,

    // One Vehicle can have many Notes
    @OneToMany(mappedBy = "vehicle", cascade = [CascadeType.ALL], orphanRemoval = true)
    var notes: MutableSet<Note> = mutableSetOf(),

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

    // Notes
    fun addNote(note: Note) {
        notes.add(note)
        note.vehicle = this
    }

    fun removeNote(note: Note) {
        notes.remove(note)
        note.vehicle = null
    }

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
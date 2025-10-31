package com.rentalcarsystem.reservationservice.models

import com.rentalcarsystem.reservationservice.enums.ReservationStatus
import jakarta.persistence.*
import java.time.LocalDateTime

const val WAS_DELIVERY_LATE_PENALTY = 5
const val WAS_CHARGED_FEE_PENALTY = 10
const val WAS_INVOLVED_IN_ACCIDENT_PENALTY = 25
const val WAS_VEHICLE_DAMAGED_PENALTY = 3
const val WAS_VEHICLE_DIRTY_PENALTY = 3
const val NO_PROBLEM_BONUS = 5

@Entity
@Table(name = "reservations")
class Reservation(
    @Column(nullable = false)
    var customerUsername: String,

    // A Reservation belongs to one Vehicle only
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var vehicle: Vehicle? = null,

    @Column(nullable = false)
    var creationDate: LocalDateTime,

    @Column(nullable = false)
    var plannedPickUpDate: LocalDateTime,

    var actualPickUpDate: LocalDateTime? = null,

    @Column(nullable = false)
    var plannedDropOffDate: LocalDateTime,

    var actualDropOffDate: LocalDateTime? = null,

    var bufferedDropOffDate: LocalDateTime,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ReservationStatus,

    @Column(nullable = false)
    var totalAmount: Double,

    // Boolean attributes used to compute eligibility score
    var wasDeliveryLate: Boolean? = null,
    var wasChargedFee: Boolean? = null,
    var wasInvolvedInAccident: Boolean? = null,

    // Integer attributes ranging from 0 to 5 (0 = best case, 5 = worst case)
    var damageLevel: Int? = null,
    var dirtinessLevel: Int? = null,

    // Staff members who handled the reservation
    var pickUpStaffUsername : String?=null,
    var dropOffStaffUsername : String?=null,
    var updatedVehicleStaffUsername : String?=null,

    ) : BaseEntity<Long>()
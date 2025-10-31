package com.rentalcarsystem.analyticsservice.models

import com.rentalcarsystem.analyticsservice.enums.ReservationStatus
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "reservations")
class Reservation(
    @Column(nullable = false)
    var customerUsername: String,

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
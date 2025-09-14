package com.rentalcarsystem.reservationservice.dtos.response.reservation

import com.rentalcarsystem.reservationservice.models.Reservation

data class StaffReservationResDTO(
    val commonInfo: CustomerReservationResDTO,
    val customerUsername: String,
    val wasDeliveryLate: Boolean?,
    val wasChargedFee: Boolean?,
    val wasInvolvedInAccident: Boolean?,
    val damageLevel: Int?,
    val dirtinessLevel: Int?,
)

fun Reservation.toStaffReservationResDTO() = StaffReservationResDTO(
    this.toCustomerReservationResDTO(),
    this.customerUsername,
    this.wasDeliveryLate,
    this.wasChargedFee,
    this.wasInvolvedInAccident,
    this.damageLevel,
    this.dirtinessLevel,
)
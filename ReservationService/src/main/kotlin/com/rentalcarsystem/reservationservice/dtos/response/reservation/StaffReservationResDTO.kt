package com.rentalcarsystem.reservationservice.dtos.response.reservation

import com.rentalcarsystem.reservationservice.models.Reservation

data class StaffReservationResDTO(
    val commonInfo: CustomerReservationResDTO,
    val customerUsername: String,
    val wasDeliveryLate: Boolean?,
    val wasChargedFee: Boolean?,
    val wasVehicleDamaged: Boolean?,
    val wasInvolvedInAccident: Boolean?,
)

fun Reservation.toStaffReservationResDTO() = StaffReservationResDTO(
    this.toCustomerReservationResDTO(),
    this.customerUsername,
    this.wasDeliveryLate,
    this.wasChargedFee,
    this.wasVehicleDamaged,
    this.wasInvolvedInAccident
)
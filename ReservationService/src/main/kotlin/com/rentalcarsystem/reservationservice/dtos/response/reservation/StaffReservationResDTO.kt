package com.rentalcarsystem.reservationservice.dtos.response.reservation

import com.rentalcarsystem.reservationservice.models.Reservation
import java.time.LocalDateTime

data class StaffReservationResDTO(
    val commonInfo: CustomerReservationResDTO,
    val customerUsername: String,
    val bufferedDropOffDate: LocalDateTime,
    val wasDeliveryLate: Boolean?,
    val wasChargedFee: Boolean?,
    val wasInvolvedInAccident: Boolean?,
    val damageLevel: Int?,
    val dirtinessLevel: Int?,
    val pickUpStaffUsername: String?,
    val dropOffStaffUsername: String?,
    val updatedVehicleStaffUsername: String?,
)

fun Reservation.toStaffReservationResDTO() = StaffReservationResDTO(
    commonInfo = this.toCustomerReservationResDTO(),
    customerUsername = this.customerUsername,
    bufferedDropOffDate = this.bufferedDropOffDate,
    wasDeliveryLate = this.wasDeliveryLate,
    wasChargedFee = this.wasChargedFee,
    wasInvolvedInAccident = this.wasInvolvedInAccident,
    damageLevel = this.damageLevel,
    dirtinessLevel = this.dirtinessLevel,
    pickUpStaffUsername = this.pickUpStaffUsername,
    dropOffStaffUsername = this.dropOffStaffUsername,
    updatedVehicleStaffUsername = this.updatedVehicleStaffUsername,
)
package com.rentalcarsystem.analyticsservice.kafka

import com.rentalcarsystem.analyticsservice.enums.EventType
import com.rentalcarsystem.analyticsservice.enums.ReservationStatus
import com.rentalcarsystem.analyticsservice.models.Reservation
import java.time.LocalDateTime

data class ReservationEventDTO(
    val type: EventType,
    val reservation: StaffReservationResDTO
)

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

data class CustomerReservationResDTO(
    val id: Long,
    val vehicleId: Long,
    val licensePlate: String,
    val vin: String,
    val brand: String,
    val model: String,
    val year: String,
    val carModelId: Long,
    val creationDate: LocalDateTime,
    val plannedPickUpDate: LocalDateTime,
    val actualPickUpDate: LocalDateTime?,
    val plannedDropOffDate: LocalDateTime,
    val actualDropOffDate: LocalDateTime?,
    val status: ReservationStatus,
    val totalAmount: Double
)

fun StaffReservationResDTO.toEntity() = Reservation(
    customerUsername = this.customerUsername,
    creationDate = this.commonInfo.creationDate,
    plannedPickUpDate = this.commonInfo.plannedPickUpDate,
    actualPickUpDate = this.commonInfo.actualPickUpDate,
    plannedDropOffDate = this.commonInfo.plannedDropOffDate,
    actualDropOffDate = this.commonInfo.actualDropOffDate,
    bufferedDropOffDate = this.bufferedDropOffDate,
    status = this.commonInfo.status,
    totalAmount = this.commonInfo.totalAmount,
    wasDeliveryLate = this.wasDeliveryLate,
    wasChargedFee = this.wasChargedFee,
    wasInvolvedInAccident = this.wasInvolvedInAccident,
    damageLevel = this.damageLevel,
    dirtinessLevel = this.dirtinessLevel,
    pickUpStaffUsername = this.pickUpStaffUsername,
    dropOffStaffUsername = this.dropOffStaffUsername,
    updatedVehicleStaffUsername = this.updatedVehicleStaffUsername
)

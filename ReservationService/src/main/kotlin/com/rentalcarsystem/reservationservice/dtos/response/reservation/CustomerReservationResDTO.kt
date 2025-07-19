package com.rentalcarsystem.reservationservice.dtos.response.reservation

import com.rentalcarsystem.reservationservice.enums.ReservationStatus
import com.rentalcarsystem.reservationservice.models.Reservation
import java.time.LocalDateTime

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

fun Reservation.toCustomerReservationResDTO() = CustomerReservationResDTO(
    this.getId()!!,
    this.vehicle?.getId()!!,
    this.vehicle!!.licensePlate,
    this.vehicle!!.vin,
    this.vehicle!!.carModel.brand,
    this.vehicle!!.carModel.model,
    this.vehicle!!.carModel.year,
    this.vehicle!!.carModel.getId()!!,
    this.creationDate,
    this.plannedPickUpDate,
    this.actualPickUpDate,
    this.plannedDropOffDate,
    this.actualDropOffDate,
    this.status,
    this.totalAmount
)
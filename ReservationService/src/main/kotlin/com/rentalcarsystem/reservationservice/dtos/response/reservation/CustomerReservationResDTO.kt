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
    id = this.getId()!!,
    vehicleId = this.vehicle?.getId()!!,
    licensePlate = this.vehicle!!.licensePlate,
    vin = this.vehicle!!.vin,
    brand = this.vehicle!!.carModel.brand,
    model = this.vehicle!!.carModel.model,
    year = this.vehicle!!.carModel.year,
    carModelId = this.vehicle!!.carModel.getId()!!,
    creationDate = this.creationDate,
    plannedPickUpDate = this.plannedPickUpDate,
    actualPickUpDate = this.actualPickUpDate,
    plannedDropOffDate = this.plannedDropOffDate,
    actualDropOffDate = this.actualDropOffDate,
    status = this.status,
    totalAmount = this.totalAmount
)
package com.rentalcarsystem.reservationservice.dtos.request.reservation

import com.rentalcarsystem.reservationservice.enums.ReservationStatus
import com.rentalcarsystem.reservationservice.models.Reservation
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Positive
import java.time.LocalDateTime

data class ReservationReqDTO(
    @field:Positive(message = "Parameter 'carModelId' must be a positive integer")
    val carModelId: Long,
    @field:Future(message = "Parameter 'plannedPickupDate' must be a future date")
    val plannedPickUpDate: LocalDateTime,
    @field:Future(message = "Parameter 'plannedDropOffDate' must be a future date")
    val plannedDropOffDate: LocalDateTime,
)

fun ReservationReqDTO.toEntity(totalAmount: Double, customerUsername: String, defaultBufferDays: Long) = Reservation(
    customerUsername = customerUsername,
    creationDate = LocalDateTime.now(),
    plannedPickUpDate = plannedPickUpDate,
    plannedDropOffDate = plannedDropOffDate,
    bufferedDropOffDate = plannedDropOffDate.plusDays(defaultBufferDays),
    status = ReservationStatus.PENDING,
    totalAmount = totalAmount
)

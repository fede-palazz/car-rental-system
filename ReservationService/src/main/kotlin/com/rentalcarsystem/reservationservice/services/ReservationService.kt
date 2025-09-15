package com.rentalcarsystem.reservationservice.services

import com.rentalcarsystem.reservationservice.dtos.request.reservation.ActualPickUpDateReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.reservation.FinalizeReservationReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.reservation.ReservationReqDTO
import com.rentalcarsystem.reservationservice.dtos.response.PagedResDTO
import com.rentalcarsystem.reservationservice.dtos.response.PaymentRecordResDTO
import com.rentalcarsystem.reservationservice.dtos.response.PaymentResDTO
import com.rentalcarsystem.reservationservice.dtos.response.reservation.StaffReservationResDTO
import com.rentalcarsystem.reservationservice.filters.PaymentRecordFilter
import com.rentalcarsystem.reservationservice.filters.ReservationFilter
import com.rentalcarsystem.reservationservice.models.Reservation
import jakarta.validation.Valid

interface ReservationService {
    fun getReservations(
        page: Int,
        size: Int,
        sortBy: String,
        sortOrder: String,
        isCustomer: Boolean,
        @Valid filters: ReservationFilter
    ): PagedResDTO<Any>

    fun createReservation(customerUsername: String, @Valid reservation: ReservationReqDTO): Any

    fun setReservationActualPickUpDate(
        pickUpStaffUsername: String,
        reservationId: Long,
        @Valid actualPickUpDate: ActualPickUpDateReqDTO
    ): StaffReservationResDTO

    fun finalizeReservation(
        dropOffStaffUsername: String,
        reservationId: Long,
        @Valid finalizeReq: FinalizeReservationReqDTO
    ): StaffReservationResDTO

    fun deleteReservation(reservationId: Long)

    fun createPaymentRequest(reservationId: Long, customerUsername: String): PaymentResDTO

    fun getPaymentRecordByReservationId(reservationId: Long): PaymentRecordResDTO

    fun getPaymentRecordByToken(token: String): PaymentRecordResDTO

    fun getReservationById(id: Long): Reservation

    fun confirmReservation(id: Long)

    fun getPaymentRecords(
        page: Int,
        size: Int,
        sortBy: String,
        sortOrder: String,
        @Valid filters: PaymentRecordFilter
    ): PagedResDTO<PaymentRecordResDTO>
}
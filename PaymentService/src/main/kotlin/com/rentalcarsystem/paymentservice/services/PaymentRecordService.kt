package com.rentalcarsystem.paymentservice.services

import com.rentalcarsystem.paymentservice.dtos.request.PaymentReqDTO
import com.rentalcarsystem.paymentservice.dtos.response.PaymentResDTO
import com.rentalcarsystem.paymentservice.dtos.response.PagedResDTO
import com.rentalcarsystem.paymentservice.dtos.response.PaymentRecordResDTO
import com.rentalcarsystem.paymentservice.filters.PaymentRecordFilter
import com.rentalcarsystem.paymentservice.models.PaymentRecord
import com.rentalcarsystem.paymentservice.models.PaymentRecordStatus
import jakarta.validation.Valid

interface PaymentRecordService {
    fun createPayPalOrder(@Valid paymentReqDTO: PaymentReqDTO): PaymentResDTO
    fun capturePayPalOrder(token: String, payerID: String)
    fun cancelPayPalOrder(token: String)
    fun getStatus(token: String): PaymentRecordStatus
    fun getPaymentRecordByReservationId(reservationId: Long): PaymentRecord
    fun getPaymentRecordByToken(token: String): PaymentRecord
    fun getPaymentRecords(
        page: Int,
        size: Int,
        sortBy: String,
        sortOrder: String,
        @Valid filters: PaymentRecordFilter,
    ): PagedResDTO<PaymentRecordResDTO>
}

interface UpdatePaymentService {
    fun setPaid(token: String)
    fun setCompleted(token: String)
    fun setCancelled(token: String)
}
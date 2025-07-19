package com.rentalcarsystem.paymentservice.dtos.response

import com.rentalcarsystem.paymentservice.models.PaymentRecord
import com.rentalcarsystem.paymentservice.models.PaymentRecordStatus

data class PaymentRecordResDTO(
    val id: Long,
    val reservationId: Long,
    val customerUsername: String,
    val amount: Double,
    val token: String?,
    val status: PaymentRecordStatus
)

fun PaymentRecord.toResDTO() = PaymentRecordResDTO(
    this.getId()!!,
    this.reservationId,
    this.customerUsername,
    this.amount,
    this.token,
    this.status
)

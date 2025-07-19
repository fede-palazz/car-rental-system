package com.rentalcarsystem.reservationservice.filters

import jakarta.validation.constraints.Positive

data class PaymentRecordFilter(
    @field:Positive(message = "Parameter 'reservationId' must be a positive number")
    val reservationId: Long? = null,
    @field:Positive(message = "Parameter 'customerId' must be a positive number")
    val customerUsername: String? = null,
    @field:Positive(message = "Parameter 'minAmount' must be a positive number")
    val minAmount: Double? = null,
    @field:Positive(message = "Parameter 'maxAmount' must be a positive number")
    val maxAmount: Double? = null,
    val token : String? = null,
    val status: PaymentRecordStatus? = null
)

enum class PaymentRecordStatus {
    CANCELLED,
    IN_PROGRESS,
    PAID,
    COMPLETED
}
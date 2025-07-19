package com.rentalcarsystem.reservationservice.dtos.response

data class PaymentRecordResDTO(
    val id: Long,
    val reservationId: Long,
    val customerUsername: String,
    val amount: Double,
    val token: String?,
    val status: PaymentRecordStatus
)

enum class PaymentRecordStatus {
    CANCELLED,
    IN_PROGRESS,
    PAID,
    COMPLETED
}
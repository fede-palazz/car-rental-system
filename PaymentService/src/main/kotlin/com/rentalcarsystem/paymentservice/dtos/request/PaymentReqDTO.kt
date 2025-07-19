package com.rentalcarsystem.paymentservice.dtos.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class PaymentReqDTO(
    @field:Positive(message = "Parameter 'reservationId' must be a positive number")
    val reservationId: Long,

    @field:NotBlank(message = "Parameter 'description' is required")
    val description: String,

    @field:NotBlank(message = "Parameter 'customerUsername' is required")
    val customerUsername: String,

    @field:Positive(message = "Parameter 'amount' must be a positive number")
    val amount: Double,
)

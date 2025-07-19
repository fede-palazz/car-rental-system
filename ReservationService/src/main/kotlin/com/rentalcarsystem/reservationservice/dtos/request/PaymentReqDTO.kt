package com.rentalcarsystem.reservationservice.dtos.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class PaymentReqDTO(
    @field:Positive(message = "Parameter 'reservationId' must be a positive number")
    val reservationId: Long,

    var description: String? = null,

    var customerUsername: String?=null,

    var amount: Double? = null,
)

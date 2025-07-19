package com.rentalcarsystem.reservationservice.kafka

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class PayPalOutboxEvent(
    val id: Long,

    @JsonProperty("payment_id")
    val paymentId: Long,

    @JsonProperty("paypal_token")
    val paypalToken: String,

    @JsonProperty("payer_id")
    val payerId: String,

    @JsonProperty("reservation_id")
    val reservationId: Long,

    @JsonProperty("created_at")
    val createdAt: Instant = Instant.now(),
)
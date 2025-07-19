package com.rentalcarsystem.paymentservice.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "paypal_outbox_events")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PayPalOutboxEvent(
    @Column(nullable = false)
    @JsonProperty("payment_id")
    val paymentId: Long,

    @Column(nullable = false)
    @JsonProperty("paypal_token")
    val paypalToken: String,

    @Column(nullable = false)
    @JsonProperty("payer_id")
    val payerId: String,

    @Column(nullable = false)
    @JsonProperty("reservation_id")
    val reservationId: Long,

    @Column(nullable = false)
    @JsonProperty("created_at")
    val createdAt: Instant = Instant.now(),
) : BaseEntity<Long>()
package com.rentalcarsystem.paymentservice.models

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table

@Entity
@Table(name = "payment_records")
class PaymentRecord(
    var reservationId: Long = 0L,

    var customerUsername: String = "",

    var amount: Double = 0.0,

    @JsonIgnore
    var token : String? = null,

    @Enumerated(EnumType.STRING)
    var status: PaymentRecordStatus = PaymentRecordStatus.IN_PROGRESS
) : BaseEntity<Long>()

enum class PaymentRecordStatus {
    CANCELLED,
    IN_PROGRESS,
    PAID,
    COMPLETED
}
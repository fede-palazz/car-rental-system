package com.rentalcarsystem.paymentservice.repositories

import com.rentalcarsystem.paymentservice.models.PayPalOutboxEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PayPalOutboxRepository : JpaRepository<PayPalOutboxEvent, Long> {
}
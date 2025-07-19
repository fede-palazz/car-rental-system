package com.rentalcarsystem.paymentservice.repositories

import com.rentalcarsystem.paymentservice.models.PaymentRecord
import com.rentalcarsystem.paymentservice.models.PaymentRecordStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.*

interface PaymentRecordRepository : JpaRepository<PaymentRecord,Long>, JpaSpecificationExecutor<PaymentRecord> {
    fun findByToken(token:String): Optional<PaymentRecord>
    fun existsByReservationIdAndStatusNot(reservationId: Long, status: PaymentRecordStatus): Boolean
    fun findByReservationId(reservationId: Long): Optional<PaymentRecord>
}
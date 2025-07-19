package com.rentalcarsystem.reservationservice.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.rentalcarsystem.reservationservice.enums.ReservationStatus
import com.rentalcarsystem.reservationservice.services.ReservationService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.slf4j.LoggerFactory
import java.time.Duration

@Component
class PayPalOrderListener(
    private val objectMapper: ObjectMapper,
    private val reservationService: ReservationService
) {
    private val logger = LoggerFactory.getLogger(PayPalOrderListener::class.java)

    @KafkaListener(topics = ["paypal.public.payments"], groupId = "\${spring.kafka.consumer.group-id:paymentService}")
    @Transactional
    fun processPayPalOrder(message: String, @Header(KafkaHeaders.ACKNOWLEDGMENT) ack: Acknowledgment) {
        try {
            if (message.isEmpty()) {
                ack.acknowledge()
                return;
            }
            // Parse the message from Kafka using the schema
            val eventData = objectMapper.readValue(message, PayPalOutboxEvent::class.java)
            val reservationId = eventData.reservationId
            val reservation = reservationService.getReservationById(reservationId)

            if (reservation.status == ReservationStatus.PENDING) {
                // Mark the reservation as paid
                reservationService.confirmReservation(reservationId)
                ack.acknowledge()
                return
            }

            if (reservation.status == ReservationStatus.CONFIRMED) {
                // Already processed, ack the message
                ack.acknowledge()
                return
            }

            if (reservation.status == ReservationStatus.EXPIRED) {
                logger.error("Error while confirming reservation with id $reservationId: reservation is expired")
                ack.acknowledge()
                return
            }

            if (reservation.status == ReservationStatus.CANCELLED) {
                logger.error("Error while confirming reservation with id $reservationId: reservation is cancelled")
                ack.acknowledge()
                return
            }
        } catch (e: Exception) {
            ack.nack(Duration.ofSeconds(5))
            // Log the error and don't acknowledge the message, so it will be retried
            logger.error("Error processing reservation: ${e.message}")
        }
    }
}
package com.rentalcarsystem.paymentservice.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.paypal.sdk.models.OrderStatus
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import com.rentalcarsystem.paymentservice.models.PayPalOutboxEvent
import com.rentalcarsystem.paymentservice.models.PaymentRecordStatus
import com.rentalcarsystem.paymentservice.services.PayPalService
import com.rentalcarsystem.paymentservice.services.PaymentRecordService
import com.rentalcarsystem.paymentservice.services.UpdatePaymentService
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import java.time.Duration

@Component
class PayPalOrderListener(
    private val payPalService: PayPalService,
    private val paymentRecordService: PaymentRecordService,
    private val updatePaymentService: UpdatePaymentService,
    private val objectMapper: ObjectMapper,
    private val kafkaTemplate: KafkaTemplate<String, String>

) {
    private val logger = LoggerFactory.getLogger(PayPalOrderListener::class.java)

    @KafkaListener(topics = ["paypal.public.paypal_outbox_events"], groupId = "\${spring.kafka.consumer.group-id:paymentService}")
    @Transactional
    fun processPayPalOrder(message: String, @Header(KafkaHeaders.ACKNOWLEDGMENT) ack: Acknowledgment) {
        try {
            if (message.isEmpty()) {
                ack.acknowledge()
                return;
            }
            // Parse the message from Kafka using the schema
            val eventData = objectMapper.readValue(message, PayPalOutboxEvent::class.java)

            val paypalToken = eventData.paypalToken
            val payerId = eventData.payerId

            when (val paymentRecordStatus = paymentRecordService.getStatus(paypalToken)) {
                PaymentRecordStatus.COMPLETED -> {
                    //already processed, ack the message
                    ack.acknowledge()
                }

                PaymentRecordStatus.PAID -> {
                    //send http request, set db and ack the message
                    val order = payPalService.captureOrder(paypalToken, payerId)
                    if (order.status == OrderStatus.COMPLETED) {
                        updatePaymentService.setCompleted(paypalToken)

                        try {
                            val future = kafkaTemplate.send("paypal.public.payments", paypalToken, message)
                            future.get()  // blocks the thread until Kafka acknowledges write
                            ack.acknowledge()
                        } catch (ex: Exception) {
                            logger.error("Failed to send payment completion event", ex)
                            ack.nack(Duration.ofSeconds(5))
                        }
                    }
                }
                else -> {
                    // Order cancelled
                    logger.warn("Cancelling PayPal order event with token $paypalToken and status $paymentRecordStatus")
                    updatePaymentService.setCancelled(paypalToken)
                    ack.acknowledge()
                }
            }
        } catch (e: Exception) {
            ack.nack(Duration.ofSeconds(5))
            // Log the error and don't acknowledge the message, so it will be retried
            logger.error("Error processing PayPal order: ${e.message}")
        }
    }
}
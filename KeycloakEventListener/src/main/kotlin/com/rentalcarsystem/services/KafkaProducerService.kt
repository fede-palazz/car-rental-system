package com.rentalcarsystem.services

import com.rentalcarsystem.model.UserEventPayload
import io.vertx.core.impl.logging.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {

    @Value("\${kafka.topic.user-events}")
    private lateinit var userEventsTopic: String

    private val logger = LoggerFactory.getLogger(KafkaProducerService::class.java)

    fun publishUserEvent(event: UserEventPayload) {
        try {
            kafkaTemplate.send(userEventsTopic, event.userId, event)
                .whenComplete { result, ex ->
                    if (ex != null) {
                        logger.error("Failed to send event for user ${event.userId}", ex)
                    } else {
                        logger.info("User event published successfully: ${event.eventType} for user ${event.userId}")
                    }
                }
        } catch (e: Exception) {
            logger.error("Error publishing user event", e)
        }
    }
}
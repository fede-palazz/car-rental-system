package com.rentalcarsystem.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rentalcarsystem.model.UserEventPayload
import io.vertx.core.impl.logging.LoggerFactory
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

class KafkaProducerService private constructor() {

    private val logger = LoggerFactory.getLogger(KafkaProducerService::class.java)
    private val producer: KafkaProducer<String, String>
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private val topic: String

    init {
        val bootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "kafka:9092"
        topic = System.getenv("KAFKA_USER_EVENTS_TOPIC") ?: "keycloak.public.user_events"

        val props = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.ACKS_CONFIG, "1")
            put(ProducerConfig.RETRIES_CONFIG, 3)
            put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1)
        }

        producer = KafkaProducer(props)
        logger.info("KafkaProducerService initialized with bootstrap servers: $bootstrapServers")
    }

    fun publishUserEvent(event: UserEventPayload) {
        try {
            val json = objectMapper.writeValueAsString(event)
            val record = ProducerRecord(topic, event.userId, json)

            producer.send(record) { metadata, exception ->
                if (exception != null) {
                    logger.error("Failed to send event for user ${event.userId}", exception)
                } else {
                    logger.info("User event published successfully: ${event.eventType} for user ${event.userId} to partition ${metadata.partition()}")
                }
            }
        } catch (e: Exception) {
            logger.error("Error publishing user event", e)
        }
    }

    fun close() {
        try {
            producer.flush()
            producer.close()
        } catch (e: Exception) {
            logger.error("Error closing Kafka producer", e)
        }
    }

    companion object {
        @Volatile
        private var instance: KafkaProducerService? = null

        fun getInstance(): KafkaProducerService {
            return instance ?: synchronized(this) {
                instance ?: KafkaProducerService().also { instance = it }
            }
        }
    }

}
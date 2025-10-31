package com.rentalcarsystem.usermanagementservice.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.rentalcarsystem.usermanagementservice.dtos.request.UserReqDTO
import com.rentalcarsystem.usermanagementservice.dtos.request.UserUpdateReqDTO
import com.rentalcarsystem.usermanagementservice.models.UserRole
import com.rentalcarsystem.usermanagementservice.services.UserService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant

@Component
class KeycloakUserEventListener(
    private val objectMapper: ObjectMapper,
    private val userService: UserService
) {
    private val logger = LoggerFactory.getLogger(KeycloakUserEventListener::class.java)

    @KafkaListener(topics = ["keycloak.public.user_events"], groupId = "\${spring.kafka.consumer.group-id:paymentService}")
    @Transactional
    fun processKeycloakEvent(message: String, @Header(KafkaHeaders.ACKNOWLEDGMENT) ack: Acknowledgment) {
        try {
            if (message.isEmpty()) {
                ack.acknowledge()
                return;
            }
            // Parse the message from Kafka using the schema
            val eventData = objectMapper.readValue(message, UserEventPayload::class.java)

            val receivedPayload = objectMapper.writeValueAsString(eventData)
            logger.info("Received User Event: {}", receivedPayload)

            when (eventData.eventType) {
                "REGISTER" -> {
                    val userReq = UserReqDTO(
                        username = eventData.username,
                        firstName = eventData.firstName,
                        lastName = eventData.lastName,
                        email = eventData.email,
                        phone = eventData.userAttributes?.get("phone")!!,
                        address = eventData.userAttributes.get("address")!!,
                        role = UserRole.CUSTOMER
                    )
                    userService.addUser(userReq)
                }
                "UPDATE_PROFILE" -> {
                    val user = userService.getActualUserByUsername(eventData.username)
                    val userReq = UserUpdateReqDTO(
                        firstName = eventData.firstName,
                        lastName = eventData.lastName,
                        phone = eventData.userAttributes?.get("phone")!!,
                        address = eventData.userAttributes.get("address")!!,
                        role = null,
                        eligibilityScore = null
                    )
                    userService.updateUser(user.getId()!!, userReq)
                }
            }
            ack.acknowledge()

        } catch (e: Exception) {
            ack.nack(Duration.ofSeconds(5))
            // Log the error and don't acknowledge the message, so it will be retried
            logger.error("Error processing reservation: ${e.message}")
        }
    }
}

data class UserEventPayload (
    val eventType: String,
    val userId: String,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val timestamp: Long,
    val realmId: String,
    val clientId: String? = null,
    val ipAddress: String? = null,
    val sessionId: String? = null,
    val adminUserId: String? = null,
    val userAttributes: Map<String, String?>? = null,
    val details: Map<String, String>? = null,
    val createdAt: String = Instant.now().toString()
)
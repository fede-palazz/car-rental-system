package com.rentalcarsystem.listeners

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rentalcarsystem.model.UserEventPayload
import com.rentalcarsystem.services.KafkaProducerService
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.keycloak.events.Event
import org.keycloak.events.EventListenerProvider
import org.keycloak.events.EventType
import org.keycloak.events.admin.AdminEvent
import org.keycloak.models.KeycloakSession
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.CompletableFuture

class UserEventListenerProvider(
    private val session: KeycloakSession,
) : EventListenerProvider {

    private val kafkaProducerService = KafkaProducerService.getInstance()
    private val logger = LoggerFactory.getLogger(UserEventListenerProvider::class.java)


    override fun onEvent(event: Event) {
        when (event.type) {
            EventType.REGISTER -> {
                logger.info("User Registration Event")
                handleUserEvent(event)
            }

            EventType.UPDATE_PROFILE -> {
                logger.info("User Update profile Event")
                handleUserEvent(event)
            }

            EventType.LOGIN -> {
                logger.info("User Login Event")
            }

            else -> logger.trace("Event type: {}", event.type)
        }
    }

    override fun onEvent(adminEvent: AdminEvent, includeRepresentation: Boolean) {
        //Handle admin events if needed
        logger.info("Admin event: ${adminEvent.operationType} on ${adminEvent.resourceType}")

        // Example: Handle user creation via admin console
        if (adminEvent.operationType.name == "CREATE" &&
            adminEvent.resourceType.name == "USER"
        ) {
            logger.info("Admin event, operation=CREATE, resource=USER")
            logger.info("User details: {}", adminEvent.representation)
            try {
                //handleAdminUserCreation(adminEvent)
            } catch (e: Exception) {
                logger.error("Error processing admin event", e)
            }

        }
    }


    override fun close() {
        // Don't close the producer here as it's shared across all instances
        logger.debug("CustomEventListenerProvider closed")
    }

    private fun handleUserEvent(event: Event) {
        try {
            val realm = session.context.realm
            val user = session.users().getUserById(realm, event.userId)
            val payload = UserEventPayload(
                eventType = event.type.toString(),
                userId = event.userId,
                username = user.username,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                timestamp = event.time,
                realmId = event.realmId,
                clientId = event.clientId,
                ipAddress = event.ipAddress,
                sessionId = event.sessionId,
                userAttributes = user.attributes.mapValues { it.value.firstOrNull() },
                details = event.details
            )
            kafkaProducerService.publishUserEvent(payload)
        } catch (e: Exception) {
            logger.error("Error handling user registration event", e)
        }
    }
}
package com.rentalcarsystem.listeners

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
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
    private val webhookUrl: String,
    private val enabledEvents: Set<EventType> = setOf(EventType.REGISTER, EventType.LOGIN),
    private val retryAttempts: Int = 3,
    private val timeoutMs: Int = 5000
) : EventListenerProvider {

    companion object {
        private val logger = LoggerFactory.getLogger(UserEventListenerProvider::class.java)
        private val objectMapper = ObjectMapper().registerKotlinModule()
        private val httpClient = HttpClients.createDefault()
    }

    override fun onEvent(event: Event) {
        when (event.type) {
            event.type -> {
                logger.info("Registrato")
            }

            event.type -> {
                logger.info("Update")
            }

            event.type -> {
                logger.info("MACHINELEAJOPFHOAUHNAçNF")
            }

            else -> {
                logger.info("Stocazzo")
            }
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

    private fun handleUserRegistration(event: Event) {
        val realm = session.context.realm
        val user = session.users().getUserById(realm, event.userId)

        if (user != null) {
            val payload = UserEventPayload(
                eventType = "REGISTER",
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

            sendWebhookAsync(payload)
            logger.info("User registration event processed for: ${user.email}")
        } else {
            logger.warn("User not found for registration event: ${event.userId}")
        }
    }

    //TODO
    private fun handleUserLogin(event: Event) {
        val payload = UserEventPayload(
            eventType = "LOGIN",
            userId = event.userId,
            timestamp = event.time,
            realmId = event.realmId,
            clientId = event.clientId,
            ipAddress = event.ipAddress,
            sessionId = event.sessionId,
            details = event.details
        )

        sendWebhookAsync(payload)
        logger.info("User login event processed for: ${event.userId}")
    }


    private fun sendWebhookAsync(payload: UserEventPayload) {
        CompletableFuture.runAsync {
            sendWebhookWithRetry(payload, retryAttempts)
        }.exceptionally { ex ->
            logger.error("Async webhook sending failed", ex)
            null
        }
    }

    private fun sendWebhookWithRetry(payload: UserEventPayload, attemptsLeft: Int) {
        try {
            val success = sendWebhook(payload)
            if (!success && attemptsLeft > 1) {
                Thread.sleep(1000) // Wait 1 second before retry
                sendWebhookWithRetry(payload, attemptsLeft - 1)
            }
        } catch (e: Exception) {
            if (attemptsLeft > 1) {
                logger.warn("Webhook attempt failed, retrying... (${attemptsLeft - 1} attempts left)", e)
                Thread.sleep(1000)
                sendWebhookWithRetry(payload, attemptsLeft - 1)
            } else {
                logger.error("All webhook attempts failed for event: ${payload.eventType}", e)
            }
        }
    }

    private fun sendWebhook(payload: UserEventPayload): Boolean {
        return try {
            val jsonPayload = objectMapper.writeValueAsString(payload)

            val httpPost = HttpPost(webhookUrl).apply {
                setHeader("Content-Type", "application/json")
                setHeader("User-Agent", "Keycloak-Event-Listener/1.0")
                setHeader("X-Event-Type", payload.eventType)
                setHeader("X-User-Id", payload.userId)
                entity = StringEntity(jsonPayload, "UTF-8")
            }

            val response = httpClient.execute(httpPost)
            val statusCode = response.statusLine.statusCode

            response.close()

            when (statusCode) {
                in 200..299 -> {
                    logger.debug("Webhook sent successfully for ${payload.eventType}: $statusCode")
                    true
                }

                in 400..499 -> {
                    logger.error("Webhook client error for ${payload.eventType}: $statusCode")
                    false // Don't retry client errors
                }

                else -> {
                    logger.warn("Webhook server error for ${payload.eventType}: $statusCode")
                    false // Retry server errors
                }
            }
        } catch (e: Exception) {
            logger.error("Error sending webhook for ${payload.eventType}", e)
            false
        }
    }

    override fun close() {
        // Cleanup resources if needed
        logger.debug("CustomEventListenerProvider closed")
    }
}

data class UserEventPayload(
    val eventType: String,
    val userId: String,
    val username: String? = null,
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
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
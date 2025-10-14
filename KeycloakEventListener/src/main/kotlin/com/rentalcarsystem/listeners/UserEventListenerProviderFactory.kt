package com.rentalcarsystem.listeners


import com.rentalcarsystem.services.KafkaProducerService
import org.keycloak.Config
import org.keycloak.events.EventListenerProvider
import org.keycloak.events.EventListenerProviderFactory
import org.keycloak.events.EventType
import org.keycloak.models.KeycloakSession
import org.keycloak.models.KeycloakSessionFactory
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext

class UserEventListenerProviderFactory : EventListenerProviderFactory {

    companion object {
        private const val PROVIDER_ID = "custom-event-listener"
        private val logger = LoggerFactory.getLogger(UserEventListenerProviderFactory::class.java)
        var applicationContext: ApplicationContext? = null
    }

    private lateinit var config: EventListenerConfig

    override fun create(session: KeycloakSession): EventListenerProvider {
        val kafkaService = applicationContext?.getBean(KafkaProducerService::class.java)
            ?: throw IllegalStateException("KafkaProducerService not found in Spring context")
        return UserEventListenerProvider(kafkaService, session)
    }

    override fun init(configScope: Config.Scope) {
        logger.info("Initializing UserEventListenerProviderFactory")
        config = EventListenerConfig(
            webhookUrl = configScope.get("webhookUrl", "http://localhost:8081/api/v1/users"),
            enabledEvents = parseEnabledEvents(configScope.get("enabledEvents", "REGISTER,LOGIN,UPDATE_PROFILE")),
            retryAttempts = configScope.getInt("retryAttempts", 3),
            timeoutMs = configScope.getInt("timeoutMs", 5000)
        )

        logger.info("Custom Event Listener initialized with config: $config")

        // Validate configuration
        validateConfig()
    }

    override fun postInit(factory: KeycloakSessionFactory) {
        logger.info("Custom Event Listener post-initialization completed")
    }

    override fun close() {
        logger.info("Custom Event Listener factory closed")
    }

    override fun getId(): String = PROVIDER_ID

    private fun parseEnabledEvents(eventsString: String): Set<EventType> {
        return try {
            eventsString.split(",")
                .map { it.trim().uppercase() }
                .filter { it.isNotBlank() }
                .mapNotNull { eventName ->
                    try {
                        EventType.valueOf(eventName)
                    } catch (e: IllegalArgumentException) {
                        logger.warn("Unknown event type: $eventName")
                        null
                    }
                }
                .toSet()
                .also { events ->
                    if (events.isEmpty()) {
                        logger.warn("No valid events configured, using defaults")
                        setOf(EventType.REGISTER, EventType.LOGIN, EventType.UPDATE_PROFILE)
                    } else {
                        logger.info("Enabled events: ${events.joinToString()}")
                        events
                    }
                }
        } catch (e: Exception) {
            logger.error("Error parsing enabled events, using defaults", e)
            setOf(EventType.REGISTER, EventType.LOGIN)
        }
    }

    private fun validateConfig() {
        try {
            // Validate webhook URL format
            if (!config.webhookUrl.startsWith("http://") && !config.webhookUrl.startsWith("https://")) {
                logger.warn("Webhook URL should start with http:// or https://: ${config.webhookUrl}")
            }

            // Validate retry attempts
            if (config.retryAttempts < 1 || config.retryAttempts > 10) {
                logger.warn("Retry attempts should be between 1 and 10, got: ${config.retryAttempts}")
            }

            // Validate timeout
            if (config.timeoutMs < 1000 || config.timeoutMs > 30000) {
                logger.warn("Timeout should be between 1000ms and 30000ms, got: ${config.timeoutMs}ms")
            }

            logger.info("Configuration validation completed")
        } catch (e: Exception) {
            logger.error("Error validating configuration", e)
        }
    }
}

data class EventListenerConfig(
    val webhookUrl: String,
    val enabledEvents: Set<EventType>,
    val retryAttempts: Int,
    val timeoutMs: Int
) {
    override fun toString(): String {
        return "EventListenerConfig(webhookUrl='$webhookUrl', " +
                "enabledEvents=${enabledEvents.joinToString()}, " +
                "retryAttempts=$retryAttempts, " +
                "timeoutMs=${timeoutMs}ms)"
    }
}
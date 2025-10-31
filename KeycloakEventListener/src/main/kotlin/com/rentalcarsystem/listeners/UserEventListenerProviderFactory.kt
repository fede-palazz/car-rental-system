package com.rentalcarsystem.listeners


import org.keycloak.Config
import org.keycloak.events.EventListenerProvider
import org.keycloak.events.EventListenerProviderFactory
import org.keycloak.models.KeycloakSession
import org.keycloak.models.KeycloakSessionFactory
import org.slf4j.LoggerFactory

class UserEventListenerProviderFactory : EventListenerProviderFactory {

    companion object {
        private const val PROVIDER_ID = "custom-event-listener"
        private val logger = LoggerFactory.getLogger(UserEventListenerProviderFactory::class.java)
    }

    override fun create(session: KeycloakSession): EventListenerProvider {
        return UserEventListenerProvider(session)
    }

    override fun init(configScope: Config.Scope) {
        logger.info("Initializing UserEventListenerProviderFactory")
    }

    override fun postInit(factory: KeycloakSessionFactory) {
    }

    override fun close() {
        logger.info("Custom Event Listener factory closed")
    }

    override fun getId(): String = PROVIDER_ID

}

package com.rentalcarsystem

import com.rentalcarsystem.listeners.UserEventListenerProviderFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@SpringBootApplication
class KeycloakEventListenerApplication {

    @Bean
    fun initializeListenerFactory(applicationContext: ApplicationContext) {
        UserEventListenerProviderFactory.applicationContext = applicationContext
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<KeycloakEventListenerApplication>(*args)
        }
    }
}

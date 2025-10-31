package com.rentalcarsystem.reservationservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {
    @Bean
    fun userManagementRestClient(@Value("\${userManagement.baseurl}") userUrl: String): RestClient {
        return RestClient.builder()
            .baseUrl(userUrl)
            .build()
    }

    @Bean
    fun paymentServiceRestClient(@Value("\${payment.baseurl}") userUrl: String): RestClient {
        return RestClient.builder()
            .baseUrl(userUrl)
            .build()
    }

    @Bean
    fun trackingServiceRestClient(@Value("\${tracking.baseurl}") trackingUrl: String): RestClient {
        return RestClient.builder()
            .baseUrl(trackingUrl)
            .build()
    }

    @Bean
    fun keycloakTokenRestClient(@Value("\${spring.security.oauth2.client.provider.keycloak.token-uri}") tokenUrl: String): RestClient{
        return RestClient.builder()
            .baseUrl(tokenUrl)
            .build()
}
}
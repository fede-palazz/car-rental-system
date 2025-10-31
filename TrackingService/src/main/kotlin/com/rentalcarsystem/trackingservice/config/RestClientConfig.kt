package com.rentalcarsystem.trackingservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {

    @Bean
    fun osrmServiceRestClient(@Value("\${osrm.baseurl}") userUrl: String): RestClient {
        return RestClient.builder()
            .baseUrl(userUrl)
            .build()
    }

}
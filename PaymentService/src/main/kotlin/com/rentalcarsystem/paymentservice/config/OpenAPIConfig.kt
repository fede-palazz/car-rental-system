package com.rentalcarsystem.paymentservice.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean

@Configuration
class OpenAPIConfig {
    @Bean
    fun getOpenApiDocumentation(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Payment Service API")
                .description("API for Payment service")
                .version("v1")
        )

}
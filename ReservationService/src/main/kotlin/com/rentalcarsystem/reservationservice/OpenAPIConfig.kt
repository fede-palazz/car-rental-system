package com.rentalcarsystem.reservationservice

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
                .title("Car Rental Agency API")
                .description("API for car rental service")
                .version("v1")
        )

}
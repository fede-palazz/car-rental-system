package com.rentalcarsystem.usermanagementservice

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
                .title("Users Management API")
                .description("API for Users Management service")
                .version("v1")
        )

}
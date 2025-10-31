package com.rentalcarsystem.analyticsservice.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.filter.ForwardedHeaderFilter

@Component
class SwaggerConfig() {

    @Bean
    fun openApiConfiguration(): OpenAPI {
        return OpenAPI().info(
            Info().title("AnalyticsService")
        )
    }

    fun forwardHeaderFilter(): ForwardedHeaderFilter {
        return ForwardedHeaderFilter()
    }
}
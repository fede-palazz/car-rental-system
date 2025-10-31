package com.rentalcarsystem.reservationservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "email")
data class EmailProperties(
    var from: String = "",
    var fromName: String = ""
)
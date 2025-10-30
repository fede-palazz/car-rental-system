package com.rentalcarsystem.apigateway

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@EnableWebSecurity
@Configuration
class SecurityConfig(val crr: ClientRegistrationRepository) {

    fun oidcLogoutSuccessHandler() = OidcClientInitiatedLogoutSuccessHandler(crr).also {
        it.setPostLogoutRedirectUri("{baseUrl}/ui?logout")
    }

    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        httpSecurity.cors {}
            .authorizeHttpRequests {
                it.requestMatchers("/gateway-client").authenticated()
                it.anyRequest().permitAll()
            }
            .oauth2Login {
                it.successHandler { _, response, _ ->
                    response.sendRedirect("/ui")

                }
                it.failureUrl("/ui?error=login")
            }
            .csrf {
                it.ignoringRequestMatchers("/logout")
            }
            .logout {
                it.logoutSuccessHandler(oidcLogoutSuccessHandler())
            }

        return httpSecurity.build()
    }


    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowedOrigins = listOf("http://localhost:4173") // Replace with your frontend origin
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        config.allowedHeaders = listOf("*")
        config.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }
}
package com.rentalcarsystem.apigateway.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeController {

    @GetMapping("/gateway-client")
    fun login(request: HttpServletRequest, httpServletResponse: HttpServletResponse) {
        httpServletResponse.sendRedirect("/ui")
    }


    @GetMapping("/gateway-client/update-profile")
    fun updateProfile(request: HttpServletRequest, response: HttpServletResponse) {
        val keycloakAccountUrl =
            "http://localhost:9090/realms/car-rental-system/account?referrer=gateway-client&referrer_uri=http://localhost:8083/ui"
        response.sendRedirect(keycloakAccountUrl)
    }

    @GetMapping("/")
    fun home(httpServletResponse: HttpServletResponse) {
        httpServletResponse.sendRedirect("/ui")
    }

    @GetMapping("/me")
    fun me(authentication: Authentication?, csrfToken: CsrfToken): Map<String, Any?> {
        if (authentication != null) {
            val user = authentication.principal as OidcUser
            return mapOf(
                "name" to user.preferredUsername,
                "userInfo" to user.userInfo,
                "csrf" to csrfToken.token,
            )
        } else {
            return mapOf(
                "error" to "User not authenticated",
                "csrf" to csrfToken.token,
            )
        }
    }

}
package com.rentalcarsystem.paymentservice

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Controller
class WebController {

    @Value("\${client.baseUrl}")
    private val clientBaseUrl: String? = null

    @GetMapping("/", "/orders/{token}")
    fun forwardReactRoutes(
        @PathVariable token: String?,
        @RequestParam("reservationId", required = false) reservationId: String?,
        @RequestParam("completed", required = false) completed: Boolean?
    ): String {
        val uri = "$clientBaseUrl/reservations"
        println("reservationId: $reservationId, completed: $completed")
        if (reservationId != null && completed != null) {
            return "redirect:$uri?reservationId=$reservationId&completed=$completed"
        }
        return "redirect:$uri"
    }
}
package com.rentalcarsystem.apigateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ApiGatewayApplication

fun main(args: Array<String>) {
    runApplication<ApiGatewayApplication>(*args)
}

//@Controller
//class UIController {
//    @GetMapping("/", "")
//    fun getUI(): String {
//        return "redirect:/ui/"
//    }
//}
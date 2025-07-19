package com.rentalcarsystem.reservationservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@EnableScheduling
@SpringBootApplication
class ReservationServiceApplication

fun main(args: Array<String>) {
    runApplication<ReservationServiceApplication>(*args)
}


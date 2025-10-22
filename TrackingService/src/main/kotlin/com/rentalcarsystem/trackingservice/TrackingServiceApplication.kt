package com.rentalcarsystem.trackingservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class TrackingServiceApplication

fun main(args: Array<String>) {
    runApplication<TrackingServiceApplication>(*args)
}

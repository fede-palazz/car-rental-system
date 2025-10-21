package com.rentalcarsystem.analyticsservice.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.rentalcarsystem.analyticsservice.dtos.response.ReservationsCountResDTO
import com.rentalcarsystem.analyticsservice.enums.Granularity
import com.rentalcarsystem.analyticsservice.services.ReservationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

const val RESERVATION_BASE_URL = "/api/v1/analytics/reservations"

@RestController
@RequestMapping(RESERVATION_BASE_URL)
@CrossOrigin(origins = ["http://localhost:8083"], allowCredentials = "true")
class ReservationController(
    private val reservationService: ReservationService
) {
    private val logger = LoggerFactory.getLogger(ReservationController::class.java)
    private val mapper = ObjectMapper().apply { registerModule(JavaTimeModule()) }

    @Operation(
        summary = "Get reservations count",
        description = "Returns the amount of reservations created during the desired date range with the desired granularity",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(
                        schema = Schema(implementation = ReservationsCountResDTO::class)
                    )
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()])
        ]
    )
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/count")
    fun getReservationsCount(
        @RequestParam("desiredStart", required = true)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) desiredStart: LocalDateTime,
        @RequestParam("desiredEnd", required = true)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) desiredEnd: LocalDateTime,
        @RequestParam("granularity", required = true) granularity: Granularity,
    ): ResponseEntity<List<ReservationsCountResDTO>> {
        require(desiredEnd.isAfter(desiredStart)) {
            "Parameter 'desiredEnd' must be after 'desiredStart'"
        }
        return ResponseEntity.ok(
            reservationService.getReservationsCount(
                desiredStart, desiredEnd, granularity
            )
        )
    }
}
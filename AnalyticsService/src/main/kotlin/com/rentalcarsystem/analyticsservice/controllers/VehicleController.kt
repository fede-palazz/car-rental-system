package com.rentalcarsystem.analyticsservice.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.rentalcarsystem.analyticsservice.dtos.response.VehicleStatusCountResDTO
import com.rentalcarsystem.analyticsservice.services.VehicleService
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
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/analytics/vehicles")
@CrossOrigin(origins = ["http://localhost:8083"], allowCredentials = "true")
class VehicleController(
    private val vehicleService: VehicleService
) {
    private val logger = LoggerFactory.getLogger(VehicleController::class.java)
    private val mapper = ObjectMapper().apply { registerModule(JavaTimeModule()) }

    @Operation(
        summary = "Get vehicles status amounts",
        description = "Returns the amount of vehicles in each status at the moment of the given date",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(
                        schema = Schema(implementation = VehicleStatusCountResDTO::class)
                    )
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "401", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()])
        ]
    )
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/status/date")
    fun getVehicleStatusCount(
        @RequestParam("desiredDate", required = true)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) desiredDate: LocalDate,
    ): ResponseEntity<VehicleStatusCountResDTO> {
        require(!desiredDate.isAfter(LocalDate.now())) {
            "Parameter 'desiredDate' must be on or before the current date and time"
        }
        return ResponseEntity.ok(
            vehicleService.getVehicleStatusCount(desiredDate)
        )
    }
}
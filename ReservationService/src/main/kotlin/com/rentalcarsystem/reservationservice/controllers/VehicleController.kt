package com.rentalcarsystem.reservationservice.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.rentalcarsystem.reservationservice.dtos.request.VehicleReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.VehicleUpdateReqDTO
import com.rentalcarsystem.reservationservice.dtos.response.PagedResDTO
import com.rentalcarsystem.reservationservice.dtos.response.VehicleResDTO
import com.rentalcarsystem.reservationservice.dtos.response.toResDTO
import com.rentalcarsystem.reservationservice.filters.VehicleFilter
import com.rentalcarsystem.reservationservice.services.VehicleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/vehicles")
@CrossOrigin(origins = ["http://localhost:8083"], allowCredentials = "true")
class VehicleController(private val vehicleService: VehicleService) {
    private val logger = LoggerFactory.getLogger(MaintenanceController::class.java)
    private val mapper = ObjectMapper().apply { registerModule(JavaTimeModule()) }

    @Operation(
        summary = "Get vehicles",
        description = "Retrieves all vehicles based on the specified query parameters",
        responses = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = PagedResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @GetMapping("")
    fun getVehicles(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
        @RequestParam("sort", defaultValue = "vin") sortBy: String,
        @RequestParam("order", defaultValue = "asc") sortOrder: String,
        @ModelAttribute filters: VehicleFilter
    ): ResponseEntity<PagedResDTO<VehicleResDTO>> {
        // Validate filters
        if (page < 0) {
            throw IllegalArgumentException("Parameter 'page' must be greater than or equal to zero")
        }
        if (size < 1) {
            throw IllegalArgumentException("Parameter 'size' must be greater than zero")
        }
        // Retrieve VehicleFilter fields' names
        val allowedSortFields = listOf(
            "licensePlate",
            "vin",
            "brand",
            "model",
            "year",
            "status",
            "kmTravelled",
            "pendingCleaning",
            "pendingRepair"
        )
        if (sortBy !in allowedSortFields) {
            throw IllegalArgumentException("Parameter 'sort' invalid. Allowed values: $allowedSortFields")
        }
        if (sortOrder !in listOf("asc", "desc")) {
            throw IllegalArgumentException("Parameter 'sortOrder' invalid. Allowed values: ['asc', 'desc']")
        }
        return ResponseEntity.ok(
            vehicleService.getVehicles(
                page, size, sortBy, sortOrder, filters
            )
        )
    }

    @Operation(
        summary = "Get vehicle by id",
        description = "Retrieves a vehicle having the specified id or throws an exception if the vehicle is not found",
        responses = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = VehicleResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @GetMapping("/{vehicleId}")
    fun getVehicleById(@PathVariable vehicleId: Long): ResponseEntity<VehicleResDTO> {
        if (vehicleId <= 0) {
            throw IllegalArgumentException("Invalid vehicle id $vehicleId: it must be a positive number")
        }
        return ResponseEntity.ok(vehicleService.getVehicleById(vehicleId).toResDTO())
    }

    @Operation(
        summary = "Get available vehicles",
        description = "Retrieves all available vehicles based on the specified car model and date range",
        responses = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = PagedResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @GetMapping("/available")
    fun getAvailableVehicles(
        @RequestParam("carModelId", required = true) carModelId: Long,
        @RequestParam("desiredStart", required = true)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) desiredStart: LocalDateTime,
        @RequestParam("desiredEnd", required = true)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) desiredEnd: LocalDateTime,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
        @RequestParam("sort", defaultValue = "vin") sortBy: String,
        @RequestParam("order", defaultValue = "asc") sortOrder: String,
    ): ResponseEntity<PagedResDTO<VehicleResDTO>> {
        // Validate filters
        require(carModelId > 0) {
            "Invalid car model id $carModelId: it must be a positive number"
        }
        require(desiredEnd.isAfter(desiredStart)) {
            "Parameter 'desiredEnd' must be after 'desiredStart'"
        }
        require(page >= 0) { "Parameter 'page' must be greater than or equal to zero" }
        require(size > 0) { "Parameter 'size' must be greater than zero" }
        // Retrieve Vehicle fields' names
        val allowedSortFields = listOf(
            "licensePlate",
            "vin",
            "status",
            "kmTravelled",
            "pendingCleaning",
            "pendingRepair"
        )
        if (sortBy !in allowedSortFields) {
            throw IllegalArgumentException("Parameter 'sort' invalid. Allowed values: $allowedSortFields")
        }
        if (sortOrder !in listOf("asc", "desc")) {
            throw IllegalArgumentException("Parameter 'sortOrder' invalid. Allowed values: ['asc', 'desc']")
        }
        return ResponseEntity.ok(
            vehicleService.getAvailableVehicles(
                carModelId, desiredStart, desiredEnd, page, size, sortBy, sortOrder
            )
        )
    }


    @Operation(
        summary = "Add vehicle",
        description = "Adds a new car instance to the fleet, linking it to a specific model from the car models catalogue",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = VehicleReqDTO::class)
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "201", content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = VehicleResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "422", content = [Content()]),
            ApiResponse(responseCode = "409", content = [Content()]),
        ]
    )
    @PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @PostMapping("")
    fun addVehicle(
        @RequestBody vehicle: VehicleReqDTO,
        uriBuilder: UriComponentsBuilder
    ): ResponseEntity<VehicleResDTO> {
        val createdVehicle = vehicleService.addVehicle(vehicle)
        logger.info("Created vehicle: {}", mapper.writeValueAsString(createdVehicle))
        val location = uriBuilder
            .path("/api/v1/vehicles/{id}")
            .buildAndExpand(createdVehicle.id)
            .toUri()
        return ResponseEntity
            .created(location) // sets status 201 and Location header
            .body(createdVehicle)
    }

    @Operation(
        summary = "Update vehicle by id",
        description = "Updates a vehicle having the specified id or throws an exception if the vehicle is not found",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = VehicleResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @PutMapping("/{vehicleId}")
    fun updateVehicle(
        @PathVariable vehicleId: Long,
        @RequestBody vehicleDTO: VehicleUpdateReqDTO
    ): ResponseEntity<VehicleResDTO> {
        if (vehicleId <= 0) {
            throw IllegalArgumentException("Invalid vehicle id $vehicleId: it must be a positive number")
        }
        val updatedVehicle = vehicleService.updateVehicle(vehicleId, vehicleDTO)
        logger.info("Updated vehicle {}: {}", vehicleId, mapper.writeValueAsString(updatedVehicle))
        return ResponseEntity.ok(updatedVehicle)
    }

    @Operation(
        summary = "Delete vehicle by id",
        description = "Deletes a vehicle having the specified id or throws an exception if the vehicle is not found",
        responses = [
            ApiResponse(responseCode = "204", content = [Content()]),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @DeleteMapping("/{vehicleId}")
    fun deleteVehicle(@PathVariable vehicleId: Long): ResponseEntity<Unit> {
        if (vehicleId <= 0) {
            throw IllegalArgumentException("Invalid vehicle id $vehicleId: it must be a positive number")
        }
        vehicleService.deleteVehicle(vehicleId)
        logger.info("Deleted vehicle {}", vehicleId)
        return ResponseEntity.noContent().build()
    }
}
package com.rentalcarsystem.reservationservice.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.rentalcarsystem.reservationservice.dtos.request.MaintenanceReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.FinalizeMaintenanceReqDTO
import com.rentalcarsystem.reservationservice.dtos.response.MaintenanceResDTO
import com.rentalcarsystem.reservationservice.dtos.response.PagedResDTO
import com.rentalcarsystem.reservationservice.exceptions.FailureException
import com.rentalcarsystem.reservationservice.exceptions.ResponseEnum
import com.rentalcarsystem.reservationservice.filters.MaintenanceFilter
import com.rentalcarsystem.reservationservice.services.MaintenanceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder


const val MAINTENANCE_BASE_URL = "/api/v1/vehicles/{vehicleId}/maintenances"

@RestController
@RequestMapping(MAINTENANCE_BASE_URL)
@CrossOrigin(origins = ["http://localhost:8083"], allowCredentials = "true")
class MaintenanceController(private val maintenanceService: MaintenanceService) {
    private val logger = LoggerFactory.getLogger(MaintenanceController::class.java)
    private val mapper = ObjectMapper().apply { registerModule(JavaTimeModule()) }

    @Operation(
        summary = "Get maintenances",
        description = "Retrieves all maintenance records for a specific vehicle based on the specified query parameters",
        responses = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = PagedResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()])
        ]
    )
    @GetMapping("")
    fun getMaintenances(
        @PathVariable vehicleId: Long,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
        @RequestParam("sort", defaultValue = "defects") sortBy: String,
        @RequestParam("order", defaultValue = "asc") sortOrder: String,
        @ModelAttribute filters: MaintenanceFilter
    ): ResponseEntity<PagedResDTO<MaintenanceResDTO>> {
        if (vehicleId <= 0) {
            throw IllegalArgumentException("Invalid vehicle id $vehicleId: it must be a positive number")
        }
        // Validate filters
        if (page < 0) {
            throw IllegalArgumentException("Parameter 'page' must be greater than or equal to zero")
        }
        if (size < 1) {
            throw IllegalArgumentException("Parameter 'size' must be greater than zero")
        }
        // Retrieve MaintenanceResDTO fields' names
        val allowedSortFields = listOf(
            "defects",
            "type",
            "upcomingServiceNeeds",
            "startDate",
            "plannedEndDate",
            "actualEndDate"
        )
        if (sortBy !in allowedSortFields) {
            throw IllegalArgumentException("Parameter 'sort' invalid. Allowed values: $allowedSortFields")
        }
        if (sortOrder !in listOf("asc", "desc")) {
            throw IllegalArgumentException("Parameter 'sortOrder' invalid. Allowed values: ['asc', 'desc']")
        }
        if (filters.minStartDate != null && filters.maxStartDate != null && filters.minStartDate.isAfter(filters.maxStartDate)) {
            throw IllegalArgumentException("Parameter 'minStartDate' must be before 'maxStartDate'")
        }
        if (filters.minPlannedEndDate != null && filters.maxPlannedEndDate != null && filters.minPlannedEndDate.isAfter(filters.maxPlannedEndDate)) {
            throw IllegalArgumentException("Parameter 'minPlannedEndDate' must be before 'maxPlannedEndDate'")
        }
        if (filters.minActualEndDate != null && filters.maxActualEndDate != null && filters.minActualEndDate.isAfter(filters.maxActualEndDate)) {
            throw IllegalArgumentException("Parameter 'minActualEndDate' must be before 'maxActualEndDate'")
        }
        return ResponseEntity.ok(
            maintenanceService.getMaintenances(
                vehicleId, page, size, sortBy, sortOrder, filters
            )
        )
    }

    @Operation(
        summary = "Get maintenance by id",
        description = "Retrieves a maintenance record having the specified id or throws an exception if the vehicle or the maintenance record is not found",
        responses = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MaintenanceResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @GetMapping("/{maintenanceId}")
    fun getMaintenanceById(
        @PathVariable vehicleId: Long,
        @PathVariable maintenanceId: Long
    ): ResponseEntity<MaintenanceResDTO> {
        if (vehicleId <= 0) {
            throw IllegalArgumentException("Invalid vehicle id $vehicleId: it must be a positive number")
        }
        if (maintenanceId <= 0) {
            throw IllegalArgumentException("Invalid maintenance id $maintenanceId: it must be a positive number")
        }
        return ResponseEntity.ok(maintenanceService.getMaintenanceById(vehicleId, maintenanceId))
    }

    @Operation(
        summary = "Add vehicle's maintenance record",
        description = "Creates a new maintenance record for the specified vehicle ",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = MaintenanceReqDTO::class)
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "201", content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MaintenanceResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()])
        ]
    )
    @PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @PostMapping("")
    fun createMaintenance(
        @PathVariable vehicleId: Long,
        @RequestBody maintenanceRecord: MaintenanceReqDTO,
        uriBuilder: UriComponentsBuilder
    ): ResponseEntity<MaintenanceResDTO> {
        if (vehicleId <= 0) {
            throw IllegalArgumentException("Vehicle id must be a positive number")
        }
        require(maintenanceRecord.startDate.isBefore(maintenanceRecord.plannedEndDate)) {
            "Start date must be before planned end date"
        }
        val authentication = SecurityContextHolder.getContext().authentication

        // Extract username
        val jwt = authentication.principal as Jwt
        val username = jwt.getClaimAsString("preferred_username")
        requireNotNull(username) { FailureException(ResponseEnum.FORBIDDEN) }

        val createdMaintenance = maintenanceService.createMaintenance(vehicleId, maintenanceRecord, username)
        logger.info(
            "Created maintenance record for vehicle {}: {}",
            vehicleId,
            mapper.writeValueAsString(createdMaintenance)
        )
        val location = uriBuilder
            .path("$MAINTENANCE_BASE_URL/${createdMaintenance.id}")
            .buildAndExpand(createdMaintenance.id)
            .toUri()
        return ResponseEntity
            .created(location) // sets status 201 and Location header
            .body(createdMaintenance)
    }

    @Operation(
        summary = "Finalize maintenance",
        description = "Finalizes the given maintenance by setting the actual end date and the completed flag",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = FinalizeMaintenanceReqDTO::class)
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MaintenanceResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "409", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()])
        ]
    )
    @PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @PutMapping("{maintenanceId}/finalize")
    fun finalizeMaintenance(
        @PathVariable vehicleId: Long,
        @PathVariable maintenanceId: Long,
        @RequestBody finalizeMaintenance: FinalizeMaintenanceReqDTO
    ): ResponseEntity<MaintenanceResDTO> {
        require(vehicleId > 0) { "Invalid vehicle id $vehicleId: it must be a positive number" }
        require(maintenanceId > 0) { "Invalid maintenance id $maintenanceId: it must be a positive number" }
        val authentication = SecurityContextHolder.getContext().authentication

        // Extract username
        val jwt = authentication.principal as Jwt
        val username = jwt.getClaimAsString("preferred_username")
        requireNotNull(username) { FailureException(ResponseEnum.FORBIDDEN) }

        val updatedMaintenance = maintenanceService.finalizeMaintenance(vehicleId, maintenanceId, finalizeMaintenance, username)
        logger.info(
            "Finalized maintenance record {} for vehicle {}: {}",
            maintenanceId,
            vehicleId,
            mapper.writeValueAsString(updatedMaintenance)
        )
        return ResponseEntity.ok(updatedMaintenance)
    }

    @Operation(
        summary = "Update maintenance by id",
        description = "Updates a maintenance record having the specified id or throws an exception if the vehicle or the maintenance record is not found",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MaintenanceResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @PutMapping("/{maintenanceId}")
    fun updateMaintenance(
        @PathVariable vehicleId: Long,
        @PathVariable maintenanceId: Long,
        @RequestBody maintenance: MaintenanceReqDTO
    ): ResponseEntity<MaintenanceResDTO> {
        if (vehicleId <= 0) {
            throw IllegalArgumentException("Invalid vehicle id $vehicleId: it must be a positive number")
        }
        if (maintenanceId <= 0) {
            throw IllegalArgumentException("Invalid maintenance id $maintenanceId: it must be a positive number")
        }
        require(maintenance.startDate.isBefore(maintenance.plannedEndDate)) {
            "Start date must be before planned end date"
        }
        val updatedMaintenance = maintenanceService.updateMaintenance(vehicleId, maintenanceId, maintenance)
        logger.info(
            "Updated maintenance record {} for vehicle {}: {}",
            maintenanceId,
            vehicleId,
            mapper.writeValueAsString(updatedMaintenance)
        )
        return ResponseEntity.ok(updatedMaintenance)
    }

    @Operation(
        summary = "Delete maintenance by id",
        description = "Deletes a maintenance record having the specified id or throws an exception if the vehicle or the maintenance record is not found",
        responses = [
            ApiResponse(responseCode = "204", content = [Content()]),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @DeleteMapping("/{maintenanceId}")
    fun deleteMaintenance(
        @PathVariable vehicleId: Long,
        @PathVariable maintenanceId: Long
    ): ResponseEntity<Unit> {
        if (vehicleId <= 0) {
            throw IllegalArgumentException("Invalid vehicle id $vehicleId: it must be a positive number")
        }
        if (maintenanceId <= 0) {
            throw IllegalArgumentException("Invalid maintenance id $maintenanceId: it must be a positive number")
        }
        maintenanceService.deleteMaintenance(vehicleId, maintenanceId)
        logger.info("Deleted maintenance record {} for vehicle {}", maintenanceId, vehicleId)
        return ResponseEntity.noContent().build()
    }
}
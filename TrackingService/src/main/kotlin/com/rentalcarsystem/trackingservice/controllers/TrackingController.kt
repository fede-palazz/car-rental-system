package com.rentalcarsystem.trackingservice.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.rentalcarsystem.trackingservice.dtos.request.SessionReqDTO
import com.rentalcarsystem.trackingservice.dtos.response.PagedResDTO
import com.rentalcarsystem.trackingservice.dtos.response.SessionResDTO
import com.rentalcarsystem.trackingservice.services.TrackingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder

const val TRACKING_BASE_URL = "/api/v1/tracking"

@RestController
@RequestMapping(TRACKING_BASE_URL)
@CrossOrigin(origins = ["http://localhost:8083"], allowCredentials = "true")
class TrackingController(private val trackingService: TrackingService) {

    private val logger = LoggerFactory.getLogger(TrackingController::class.java)
    private val mapper = ObjectMapper().apply { registerModule(JavaTimeModule()) }

    @Operation(
        summary = "Get ongoing tracking sessions",
        description = "Retrieves all the ongoing tracking sessions",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(
                        schema = Schema(implementation = SessionResDTO::class)
                    )
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @GetMapping("sessions")
    fun getOngoingTrackingSessions(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
        @RequestParam("sort", defaultValue = "startDate") sortBy: String,
        @RequestParam("order", defaultValue = "desc") sortOrder: String,
    ): ResponseEntity<List<SessionResDTO>> {
        // Validate filters
        if (page < 0) {
            throw IllegalArgumentException("Parameter 'page' must be greater than or equal to zero")
        }
        if (size < 1) {
            throw IllegalArgumentException("Parameter 'size' must be greater than zero")
        }
        val allowedSortFields = listOf(
            "customerUsername",
            "startDate",
            "endDate",
        )
        if (sortBy !in allowedSortFields) {
            throw IllegalArgumentException("Parameter 'sort' invalid. Allowed values: $allowedSortFields")
        }
        if (sortOrder !in listOf("asc", "desc")) {
            throw IllegalArgumentException("Parameter 'sortOrder' invalid. Allowed values: ['asc', 'desc']")
        }
        return ResponseEntity.ok(
            trackingService.getOngoingSessions(
                page, size, sortBy, sortOrder
            )
        )
    }

    @Operation(
        summary = "Get ongoing tracking session by customer username",
        description = "Retrieves the ongoing tracking session by customer username if any, null otherwise",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = SessionResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
        ]
    )
    @GetMapping("sessions/customer/{username}")
    fun getOngoingTrackingSessionByUsername(@PathVariable username: String): ResponseEntity<SessionResDTO> {
        require(!username.isEmpty()) {
            throw IllegalArgumentException("Parameter 'username' is required")
        }
        val session = trackingService.getOngoingTrackingSessionByUsername(username)
        return ResponseEntity.ok(session)
    }

    @Operation(
        summary = "Get ongoing tracking session by reservation ID",
        description = "Retrieves the ongoing tracking session by reservation ID if any, null otherwise",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = SessionResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
        ]
    )
    @GetMapping("sessions/reservation/{reservationId}")
    fun getOngoingTrackingSessionByReservation(@PathVariable reservationId: Long): ResponseEntity<SessionResDTO> {
        require(reservationId > 0) {
            throw IllegalArgumentException("Parameter 'reservationId' is invalid")
        }
        val session = trackingService.getOngoingTrackingSessionByReservation(reservationId)
        return ResponseEntity.ok(session)
    }

    @Operation(
        summary = "Get ongoing tracking session by vehicle ID",
        description = "Retrieves the ongoing tracking session by vehicle ID if any, null otherwise",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = SessionResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
        ]
    )
    @GetMapping("sessions/vehicle/{vehicleId}")
    fun getOngoingTrackingSessionByVehicle(@PathVariable vehicleId: Long): ResponseEntity<SessionResDTO> {
        require(vehicleId > 0) {
            throw IllegalArgumentException("Parameter 'vehicleId' is invalid")
        }
        val session = trackingService.getOngoingTrackingSessionByVehicle(vehicleId)
        return ResponseEntity.ok(session)
    }

    @Operation(
        summary = "Add tracking session",
        description = "Creates a new tracking session for the specified vehicle, customer and reservation",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = SessionReqDTO::class)
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "201", content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = SessionResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "409", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    //@PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @PostMapping("sessions")
    fun createTrackingSession(
        @RequestBody sessionReq: SessionReqDTO,
        uriBuilder: UriComponentsBuilder
    ): ResponseEntity<SessionResDTO> {
        //val authentication = SecurityContextHolder.getContext().authentication

        // Extract username
//        val jwt = authentication.principal as Jwt
//        val username = jwt.getClaimAsString("preferred_username")
//        requireNotNull(username) { FailureException(ResponseEnum.FORBIDDEN) }

        val createdSession = trackingService.createTrackingSession(sessionReq)
        logger.info(
            "Created tracking session for vehicle {}: {}",
            createdSession.vehicleId,
            mapper.writeValueAsString(createdSession)
        )
        val location = uriBuilder
            .path("${TRACKING_BASE_URL}/${createdSession.id}")
            .buildAndExpand(createdSession.id)
            .toUri()
        return ResponseEntity
            .created(location) // sets status 201 and Location header
            .body(createdSession)
    }

    @Operation(
        summary = "End tracking session by id",
        description = "End the tracking of a specific session or throws an exception if the session is not found",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = SessionResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "403", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "409", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    //@PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @PutMapping("sessions/id/{id}")
    fun endTrackingSession(
        @PathVariable id: Long,
    ): ResponseEntity<SessionResDTO> {
        if (id <= 0) {
            throw IllegalArgumentException("Invalid vehicle id $id: it must be a positive number")
        }
        //val authentication = SecurityContextHolder.getContext().authentication

        // Extract username
//        val jwt = authentication.principal as Jwt
//        val username = jwt.getClaimAsString("preferred_username")
//        requireNotNull(username) { FailureException(com.rentalcarsystem.reservationservice.exceptions.ResponseEnum.FORBIDDEN) }

        val updatedSession = trackingService.endTrackingSession(id)
        logger.info("Ended tracking of session with ID: {}", id)

        return ResponseEntity.ok(updatedSession)
    }
}
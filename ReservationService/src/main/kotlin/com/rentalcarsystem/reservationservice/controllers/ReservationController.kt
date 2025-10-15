package com.rentalcarsystem.reservationservice.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.rentalcarsystem.reservationservice.dtos.request.reservation.ActualPickUpDateReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.reservation.FinalizeReservationReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.reservation.ReservationReqDTO
import com.rentalcarsystem.reservationservice.dtos.response.CarModelResDTO
import com.rentalcarsystem.reservationservice.dtos.response.PagedResDTO
import com.rentalcarsystem.reservationservice.dtos.response.PaymentRecordResDTO
import com.rentalcarsystem.reservationservice.dtos.response.PaymentResDTO
import com.rentalcarsystem.reservationservice.dtos.response.reservation.CustomerReservationResDTO
import com.rentalcarsystem.reservationservice.dtos.response.reservation.StaffReservationResDTO
import com.rentalcarsystem.reservationservice.dtos.response.reservation.toStaffReservationResDTO
import com.rentalcarsystem.reservationservice.enums.ReservationStatus
import com.rentalcarsystem.reservationservice.exceptions.FailureException
import com.rentalcarsystem.reservationservice.exceptions.ResponseEnum
import com.rentalcarsystem.reservationservice.filters.CarModelFilter
import com.rentalcarsystem.reservationservice.filters.PaymentRecordFilter
import com.rentalcarsystem.reservationservice.filters.ReservationFilter
import com.rentalcarsystem.reservationservice.services.CarModelService
import com.rentalcarsystem.reservationservice.services.ReservationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDateTime

const val RESERVATION_BASE_URL = "/api/v1/reservations"

@RestController
@RequestMapping(RESERVATION_BASE_URL)
@CrossOrigin(origins = ["http://localhost:8083"], allowCredentials = "true")
class ReservationController(
    private val reservationService: ReservationService,
    private val carModelService: CarModelService
) {
    private val logger = LoggerFactory.getLogger(MaintenanceController::class.java)
    private val mapper = ObjectMapper().apply { registerModule(JavaTimeModule()) }

    @Operation(
        summary = "Get all reservations",
        description = "Returns all reservations based on the specified query parameters with JSON schema according to the role",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(
                        schema = Schema(implementation = PagedResDTO::class)
                    )
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()])
        ]
    )
    @GetMapping("")
    fun getReservations(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
        @RequestParam("sort", defaultValue = "creationDate") sortBy: String,
        @RequestParam("order", defaultValue = "asc") sortOrder: String,
        @ModelAttribute filters: ReservationFilter
    ): ResponseEntity<PagedResDTO<Any>> {
        val authentication = SecurityContextHolder.getContext().authentication
        val authorities = authentication.authorities

        // Extract roles from authorities
        val roles = authorities
            .map { it.authority }
            .filter { it.startsWith("ROLE_") }
            .map { it.removePrefix("ROLE_") }

        val isCustomer = "CUSTOMER" in roles

        // Extract username
        val jwt = authentication.principal as Jwt
        val username = jwt.getClaimAsString("preferred_username")
        requireNotNull(username) { FailureException(ResponseEnum.FORBIDDEN) }

        if (isCustomer) {
            if (filters.customerUsername != null) {
                throw FailureException(ResponseEnum.FORBIDDEN)
            }
            filters.customerUsername = username
        }

        if (isCustomer && (filters.minBufferedDropOffDate != null || filters.maxBufferedDropOffDate != null ||
                filters.wasChargedFee != null || filters.wasDeliveryLate != null ||
                filters.wasInvolvedInAccident != null || filters.minDamageLevel != null ||
                filters.maxDamageLevel != null || filters.minDirtinessLevel != null ||
                filters.maxDirtinessLevel != null || filters.pickUpStaffUsername != null ||
                filters.dropOffStaffUsername != null)) {
            throw IllegalArgumentException("Invalid search filters")
        }

        if (page < 0) {
            throw IllegalArgumentException("Parameter 'page' must be greater than or equal to zero")
        }
        if (size < 1) {
            throw IllegalArgumentException("Parameter 'size' must be greater than zero")
        }
        val allowedCustomerSortFields = listOf(
            "licensePlate",
            "vin",
            "brand",
            "model",
            "year",
            "creationDate",
            "plannedPickUpDate",
            "actualPickUpDate",
            "plannedDropOffDate",
            "actualDropOffDate",
            "status",
            "totalAmount"
        )
        val allowedStaffSortFields = listOf(
            "customerUsername",
            "bufferedDropOffDate",
            "wasDeliveryLate",
            "wasChargedFee",
            "wasInvolvedInAccident",
            "damageLevel",
            "dirtinessLevel",
            "pickUpStaffUsername",
            "dropOffStaffUsername"
        )
        if (!isCustomer && sortBy !in allowedCustomerSortFields && sortBy !in allowedStaffSortFields) {
            throw IllegalArgumentException("Parameter 'sort' invalid. Allowed values: ${allowedCustomerSortFields + allowedStaffSortFields}")
        }
        if (isCustomer && sortBy !in allowedCustomerSortFields) {
            throw IllegalArgumentException("Parameter 'sort' invalid. Allowed values: $allowedCustomerSortFields")
        }
        if (sortOrder !in listOf("asc", "desc")) {
            throw IllegalArgumentException("Parameter 'sortOrder' invalid. Allowed values: ['asc', 'desc']")
        }
        return ResponseEntity.ok(
            reservationService.getReservations(
                page, size, sortBy, sortOrder, isCustomer, filters
            )
        )
    }

    @Operation(
        summary = "Get reservation by id",
        description = "Retrieves a reservation having the specified id or throws an exception if the reservation is not found",
        responses = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StaffReservationResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "401", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @GetMapping("/{reservationId}")
    fun getReservationById(@PathVariable reservationId: Long): ResponseEntity<StaffReservationResDTO> {
        require(reservationId > 0) { "Invalid reservation id $reservationId: it must be a positive number" }
        return ResponseEntity.ok(reservationService.getReservationById(reservationId).toStaffReservationResDTO())
    }

    @Operation(
        summary = "Search for model availability in the given date range",
        description = "Returns all models which comply to the given filters and have at least one vehicle not under maintenance and available in the given date range",
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
    @GetMapping("/search-availability")
    fun getAvailableCarModels(
        @RequestParam("desiredPickUpDate", required = true)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) desiredPickUpDate: LocalDateTime,
        @RequestParam("desiredDropOffDate", required = true)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) desiredDropOffDate: LocalDateTime,
        @RequestParam("reservationToUpdateId", required = false) reservationToUpdateId: Long?,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
        @RequestParam("singlePage", defaultValue = "false") singlePage: Boolean,
        @RequestParam("sort", defaultValue = "brand") sortBy: String,
        @RequestParam("order", defaultValue = "asc") sortOrder: String,
        @ModelAttribute filters: CarModelFilter
    ): ResponseEntity<PagedResDTO<CarModelResDTO>> {
        val authentication = SecurityContextHolder.getContext().authentication
        val authorities = authentication.authorities

        // Extract roles from authorities
        val roles = authorities
            .map { it.authority }
            .filter { it.startsWith("ROLE_") }
            .map { it.removePrefix("ROLE_") }

        val isCustomer = "CUSTOMER" in roles
        // Extract username
        val jwt = authentication.principal as Jwt
        val username = jwt.getClaimAsString("preferred_username")
        requireNotNull(username) { FailureException(ResponseEnum.FORBIDDEN) }

        require(reservationToUpdateId == null || reservationToUpdateId > 0) {
            "Invalid reservation id $reservationToUpdateId: if provided it must be a positive number"
        }
        require(desiredPickUpDate.isAfter(LocalDateTime.now())) {
            "Parameter 'desiredPickUpDate' must be in the future"
        }
        require(desiredDropOffDate.isAfter(desiredPickUpDate)) {
            "Parameter 'desiredDropOffDate' must be after 'desiredPickUpDate'"
        }
        // Validate filters
        require(page >= 0) { "Parameter 'page' must be greater than or equal to zero" }
        require(size >= 1) { "Parameter 'size' must be greater than zero" }
        // Retrieve CarModelFilter fields' names
        val allowedSortFields = listOf(
            "brand",
            "model",
            "year",
            "segment",
            "category",
            "engineType",
            "transmissionType",
            "drivetrain",
            "rentalPrice"
        )
        require(sortBy in allowedSortFields) {
            "Parameter 'sort' invalid. Allowed values: $allowedSortFields"
        }
        require(sortOrder in listOf("asc", "desc")) {
            "Parameter 'sortOrder' invalid. Allowed values: ['asc', 'desc']"
        }
        return ResponseEntity.ok(
            carModelService.getAllModels(
                page,
                size,
                singlePage,
                sortBy,
                sortOrder,
                filters,
                desiredPickUpDate,
                desiredDropOffDate,
                username,
                isCustomer
            )
        )
    }

    @Operation(
        summary = "Get overlapping reservations",
        description = "Returns all reservations belonging to the given vehicle and overlapping the given date range",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(
                        schema = Schema(implementation = PagedResDTO::class)
                    )
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()])
        ]
    )
    @PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @GetMapping("/overlapping")
    fun getOverlappingReservations(
        @RequestParam("vehicleId", required = true) vehicleId: Long,
        @RequestParam("desiredStart", required = true)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) desiredStart: LocalDateTime,
        @RequestParam("desiredEnd", required = true)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) desiredEnd: LocalDateTime,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
        @RequestParam("sort", defaultValue = "creationDate") sortBy: String,
        @RequestParam("order", defaultValue = "asc") sortOrder: String,
    ): ResponseEntity<PagedResDTO<StaffReservationResDTO>> {
        // Validate filters
        require(vehicleId > 0) {
            "Invalid vehicle id $vehicleId: it must be a positive number"
        }
        require(desiredEnd.isAfter(desiredStart)) {
            "Parameter 'desiredEnd' must be after 'desiredStart'"
        }
        require(page >= 0) { "Parameter 'page' must be greater than or equal to zero" }
        require(size > 0) { "Parameter 'size' must be greater than zero" }
        val allowedSortFields = listOf(
            "licensePlate",
            "vin",
            "brand",
            "model",
            "year",
            "creationDate",
            "plannedPickUpDate",
            "actualPickUpDate",
            "plannedDropOffDate",
            "actualDropOffDate",
            "bufferedDropOffDate",
            "status",
            "totalAmount",
            "customerUsername",
            "wasDeliveryLate",
            "wasChargedFee",
            "wasInvolvedInAccident",
            "damageLevel",
            "dirtinessLevel",
            "pickUpStaffUsername",
            "dropOffStaffUsername"
        )
        if (sortBy !in allowedSortFields) {
            throw IllegalArgumentException("Parameter 'sort' invalid. Allowed values: $allowedSortFields")
        }
        if (sortOrder !in listOf("asc", "desc")) {
            throw IllegalArgumentException("Parameter 'sortOrder' invalid. Allowed values: ['asc', 'desc']")
        }
        return ResponseEntity.ok(
            reservationService.getOverlappingReservations(
                vehicleId, desiredStart, desiredEnd, page, size, sortBy, sortOrder
            )
        )
    }

    @Operation(
        summary = "Get overlapping reservations by reservation",
        description = "Returns all reservations belonging to the given vehicle and overlapping the given date range," +
                "from the reservation's start date to the desired buffered end date.",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(
                        schema = Schema(implementation = PagedResDTO::class)
                    )
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "409", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()])
        ]
    )
    @PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @GetMapping("{reservationId}/overlapping")
    fun getOverlappingReservationsByReservationId(
        @PathVariable reservationId: Long,
        @RequestParam("bufferedDropOffDate", required = true)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) bufferedDropOffDate: LocalDateTime,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
        @RequestParam("sort", defaultValue = "creationDate") sortBy: String,
        @RequestParam("order", defaultValue = "asc") sortOrder: String,
    ): ResponseEntity<PagedResDTO<StaffReservationResDTO>> {
        require(reservationId > 0) { "Invalid reservation id $reservationId: it must be a positive number" }
        require(page >= 0) { "Parameter 'page' must be greater than or equal to zero" }
        require(size > 0) { "Parameter 'size' must be greater than zero" }
        val allowedSortFields = listOf(
            "licensePlate",
            "vin",
            "brand",
            "model",
            "year",
            "creationDate",
            "plannedPickUpDate",
            "actualPickUpDate",
            "plannedDropOffDate",
            "actualDropOffDate",
            "bufferedDropOffDate",
            "status",
            "totalAmount",
            "customerUsername",
            "wasDeliveryLate",
            "wasChargedFee",
            "wasInvolvedInAccident",
            "damageLevel",
            "dirtinessLevel",
            "pickUpStaffUsername",
            "dropOffStaffUsername"
        )
        if (sortBy !in allowedSortFields) {
            throw IllegalArgumentException("Parameter 'sort' invalid. Allowed values: $allowedSortFields")
        }
        if (sortOrder !in listOf("asc", "desc")) {
            throw IllegalArgumentException("Parameter 'sortOrder' invalid. Allowed values: ['asc', 'desc']")
        }

        val reservation = reservationService.getReservationById(reservationId)
        if (reservation.status != ReservationStatus.PICKED_UP) {
            throw FailureException(
                ResponseEnum.RESERVATION_WRONG_STATUS,
                "The vehicle of reservation $reservationId has not been picked up yet"
            )
        }
        if (bufferedDropOffDate.isBefore(reservation.actualPickUpDate)) {
            throw IllegalArgumentException("The buffered drop-off date cannot be before the actual pick-up date: ${reservation.actualPickUpDate}")
        }

        return ResponseEntity.ok(
            reservationService.getOverlappingReservations(
                vehicleId = reservation.vehicle?.getId()!!,
                desiredStart = reservation.actualPickUpDate!!,
                desiredEnd = bufferedDropOffDate,
                page = page,
                size = size,
                sortBy = sortBy,
                sortOrder = sortOrder,
                reservationToExcludeId = reservationId
            )
        )
    }

    @Operation(
        summary = "Add a new reservation",
        description = "Adds a new reservation for: the given customer, a vehicle of the given model and the given date range, and returns the created reservation with JSON schema according to the role",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ReservationReqDTO::class)
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "201", content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(
                        schema = Schema(
                            oneOf = [
                                CustomerReservationResDTO::class,
                                StaffReservationResDTO::class
                            ]
                        )
                    )
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "409", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()])
        ]
    )
    @PostMapping("")
    fun createReservation(
        @RequestBody reservation: ReservationReqDTO,
        uriBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        val authentication = SecurityContextHolder.getContext().authentication

        // Extract username
        val jwt = authentication.principal as Jwt
        val username = jwt.getClaimAsString("preferred_username")
        requireNotNull(username) { FailureException(ResponseEnum.FORBIDDEN) }

        val createdReservation = reservationService.createReservation(username, reservation)
        logger.info("Created reservation: {}", mapper.writeValueAsString(createdReservation))
        var createdReservationId: Long = 0
        if (createdReservation is CustomerReservationResDTO) {
            createdReservationId = createdReservation.id
        } else if (createdReservation is StaffReservationResDTO) {
            createdReservationId = createdReservation.commonInfo.id
        }
        val location = uriBuilder
            .path("${RESERVATION_BASE_URL}/${createdReservationId}")
            .buildAndExpand(createdReservationId)
            .toUri()
        return ResponseEntity
            .created(location) // sets status 201 and Location header
            .body(createdReservation)
    }

    @Operation(
        summary = "Set reservation's actual pick up date",
        description = "Sets the actual pick up date for the given reservation",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ActualPickUpDateReqDTO::class)
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StaffReservationResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "409", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()])
        ]
    )
    @PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @PutMapping("{reservationId}/pick-up-date")
    fun setReservationActualPickUpDate(
        @PathVariable reservationId: Long,
        @RequestBody actualPickUpDate: ActualPickUpDateReqDTO
    ): ResponseEntity<StaffReservationResDTO> {
        require(reservationId > 0) { "Invalid reservation id $reservationId: it must be a positive number" }
        val authentication = SecurityContextHolder.getContext().authentication

        // Extract username
        val jwt = authentication.principal as Jwt
        val username = jwt.getClaimAsString("preferred_username")
        requireNotNull(username) { FailureException(ResponseEnum.FORBIDDEN) }

        val updatedReservation = reservationService.setReservationActualPickUpDate(username, reservationId, actualPickUpDate)
        logger.info(
            "Set actual pick-up date for reservation {}: {}",
            reservationId,
            mapper.writeValueAsString(updatedReservation)
        )
        return ResponseEntity.ok(updatedReservation)
    }

    @Operation(
        summary = "Finalize reservation",
        description = "Finalizes the given reservation by setting the actual drop-off date and the evaluations",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = FinalizeReservationReqDTO::class)
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StaffReservationResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "409", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()])
        ]
    )
    @PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @PutMapping("{reservationId}/finalize")
    fun finalizeReservation(
        @PathVariable reservationId: Long,
        @RequestBody finalizeReservation: FinalizeReservationReqDTO
    ): ResponseEntity<StaffReservationResDTO> {
        require(reservationId > 0) { "Invalid reservation id $reservationId: it must be a positive number" }
        require(finalizeReservation.bufferedDropOffDate.isAfter(finalizeReservation.actualDropOffDate)) {
            "Parameter 'bufferedDropOffDate' must be after 'actualDropOffDate'"
        }
        val authentication = SecurityContextHolder.getContext().authentication

        // Extract username
        val jwt = authentication.principal as Jwt
        val username = jwt.getClaimAsString("preferred_username")
        requireNotNull(username) { FailureException(ResponseEnum.FORBIDDEN) }

        val updatedReservation = reservationService.finalizeReservation(username, reservationId, finalizeReservation)
        logger.info("Finalized reservation {}: {}", reservationId, mapper.writeValueAsString(updatedReservation))
        return ResponseEntity.ok(updatedReservation)
    }

    @Operation(
        summary = "Update reservation's vehicle",
        description = "Updates the vehicle of the given reservation with the given vehicle, only if it is available in the date range of such reservation",
        responses = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StaffReservationResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "409", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()])
        ]
    )
    @PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @PutMapping("{reservationId}/vehicle/{vehicleId}")
    fun updateReservationVehicle(
        @PathVariable reservationId: Long,
        @PathVariable vehicleId: Long
    ): ResponseEntity<StaffReservationResDTO> {
        require(reservationId > 0) { "Invalid reservation id $reservationId: it must be a positive number" }
        require(vehicleId > 0) { "Invalid vehicle id $vehicleId: it must be a positive number" }
        val authentication = SecurityContextHolder.getContext().authentication

        // Extract username
        val jwt = authentication.principal as Jwt
        val username = jwt.getClaimAsString("preferred_username")
        requireNotNull(username) { FailureException(ResponseEnum.FORBIDDEN) }

        val updatedReservation = reservationService.updateReservationVehicle(username, reservationId, vehicleId)
        logger.info("Updated vehicle of reservation {}: {}", reservationId, mapper.writeValueAsString(updatedReservation))
        return ResponseEntity.ok(updatedReservation)
    }

    @Operation(
        summary = "Delete reservation by id",
        description = "Deletes the reservation having the specified id or throws an exception if the reservation is not found",
        responses = [
            ApiResponse(responseCode = "204", content = [Content()]),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "403", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @DeleteMapping("{reservationId}")
    fun deleteReservation(@PathVariable reservationId: Long): ResponseEntity<Unit> {
        require(reservationId > 0) { "Invalid reservation id $reservationId: it must be a positive number" }
        reservationService.deleteReservation(reservationId)
        logger.info("Deleted reservation {}", reservationId)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "Create payment request",
        description = "Call PaymentService to create a payment request",
        responses = [
            ApiResponse(responseCode = "200", content = [Content()]),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("{reservationId}/payment-request")
    fun createPaymentRequest(
        @PathVariable reservationId: Long
    ): ResponseEntity<PaymentResDTO> {
        require(reservationId > 0) { "Invalid reservation id $reservationId: it must be a positive number" }
        val authentication = SecurityContextHolder.getContext().authentication

        // Extract username
        val jwt = authentication.principal as Jwt
        val username = jwt.getClaimAsString("preferred_username")
        requireNotNull(username) { FailureException(ResponseEnum.FORBIDDEN) }

        val paymentRes = reservationService.createPaymentRequest(reservationId, username)
        logger.info("Created payment request for reservation {}", reservationId)
        return ResponseEntity.ok(paymentRes)
    }

    @Operation(
        summary = "Get payment record by reservation id",
        description = "Retrieves a payment record having the specified reservation id or throws an exception if the payment record is not found",
        responses = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = PaymentRecordResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @GetMapping("{reservationId}/payment-request")
    fun getPaymentRecordByReservationId(@PathVariable reservationId: Long): ResponseEntity<PaymentRecordResDTO> {
        require(reservationId > 0) { "Invalid reservation id $reservationId: it must be a positive number" }
        return ResponseEntity.ok(reservationService.getPaymentRecordByReservationId(reservationId))
    }

    @Operation(
        summary = "Get payment record by token",
        description = "Retrieves a payment record having the specified token or throws an exception if the payment record is not found",
        responses = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = PaymentRecordResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @GetMapping("payment-request/token/{token}")
    fun getPaymentRecordByToken(@PathVariable token: String): ResponseEntity<PaymentRecordResDTO> {
        require(token.isNotBlank()) { "Invalid token $token: it must not be blank" }
        return ResponseEntity.ok(reservationService.getPaymentRecordByToken(token))
    }

    @Operation(
        summary = "Get all payment records",
        description = "Returns all payment records based on the specified query parameters",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(
                        schema = Schema(implementation = PagedResDTO::class)
                    )
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()])
        ]
    )
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("payment-requests")
    fun getPaymentRecords(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
        @RequestParam("sort", defaultValue = "token") sortBy: String,
        @RequestParam("order", defaultValue = "asc") sortOrder: String,
        @ModelAttribute filters: PaymentRecordFilter
    ): ResponseEntity<PagedResDTO<PaymentRecordResDTO>> {
        require(page >= 0) { "Parameter 'page' must be greater than or equal to zero" }
        require(size > 0) { "Parameter 'size' must be greater than zero" }
        val allowedSortFields = listOf(
            "reservationId",
            "customerId",
            "amount",
            "token",
            "status"
        )
        require(sortBy in allowedSortFields) { "Parameter 'sort' invalid. Allowed values: $allowedSortFields" }
        require(sortOrder in listOf("asc", "desc")) { "Parameter 'sortOrder' invalid. Allowed values: ['asc', 'desc']" }
        return ResponseEntity.ok(
            reservationService.getPaymentRecords(
                page, size, sortBy, sortOrder, filters
            )
        )
    }
}
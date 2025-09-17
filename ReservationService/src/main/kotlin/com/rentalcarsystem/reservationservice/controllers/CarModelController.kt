package com.rentalcarsystem.reservationservice.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.rentalcarsystem.reservationservice.dtos.request.CarModelReqDTO
import com.rentalcarsystem.reservationservice.dtos.response.CarFeatureResDTO
import com.rentalcarsystem.reservationservice.dtos.response.CarModelResDTO
import com.rentalcarsystem.reservationservice.dtos.response.PagedResDTO
import com.rentalcarsystem.reservationservice.filters.CarModelFilter
import com.rentalcarsystem.reservationservice.models.CarModel
import com.rentalcarsystem.reservationservice.services.CarModelService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
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

@RestController
@RequestMapping("/api/v1/models")
@CrossOrigin(origins = ["http://localhost:8083"], allowCredentials = "true")
class CarModelController(private val carModelService: CarModelService) {
    private val logger = LoggerFactory.getLogger(MaintenanceController::class.java)
    private val mapper = ObjectMapper().apply { registerModule(JavaTimeModule()) }

    @Operation(
        summary = "Get car models",
        description = "Retrieves all car models based on the specified query parameters",
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
    fun getCarModels(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
        @RequestParam("singlePage", defaultValue = "false") singlePage: Boolean,
        @RequestParam("sort", defaultValue = "brand") sortBy: String,
        @RequestParam("order", defaultValue = "asc") sortOrder: String,
        @ModelAttribute filters: CarModelFilter
    ): ResponseEntity<PagedResDTO<CarModelResDTO>> {
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

        val authentication = SecurityContextHolder.getContext().authentication
        val authorities = authentication.authorities

        // Extract roles from authorities
        val roles = authorities
            .map { it.authority }
            .filter { it.startsWith("ROLE_") }
            .map { it.removePrefix("ROLE_") }

        val isAuthenticated = "ANONYMOUS" !in roles
        var isCustomer = false
        var username: String? = null

        if (isAuthenticated) {
            isCustomer = "CUSTOMER" in roles
            // Extract username
            val jwt = authentication.principal as Jwt
            username = jwt.getClaimAsString("preferred_username")
        }

        return ResponseEntity.ok(
            carModelService.getAllModels(
                page, size, singlePage, sortBy, sortOrder, filters, customerUsername = username, isCustomer = isCustomer
            )
        )
    }

    @Operation(
        summary = "Get car model by id",
        description = "Retrieves a car model having the specified id or throws an exception if the car model is not found",
        responses = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CarModelResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
        ]
    )
    @GetMapping("{id}")
    fun getCarModel(@PathVariable("id") id: Long): ResponseEntity<CarModelResDTO> {
        require(id > 0) { "Invalid car model id $id: it must be a positive number" }
        return ResponseEntity.ok(carModelService.getCarModelById(id))
    }

    @Operation(
        summary = "Get all car features",
        description = "Retrieves all car features",
        responses = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(
                        schema = Schema(implementation = CarFeatureResDTO::class)
                    )
                )]
            )
        ]
    )
    @GetMapping("/features")
    fun getAllCarFeatures(): ResponseEntity<List<CarFeatureResDTO>> {
        return ResponseEntity.ok(carModelService.getAllCarFeatures())
    }

    @Operation(
        summary = "Get car feature by id",
        description = "Retrieves a car feature having the specified id or throws an exception if the feature is not found",
        responses = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CarFeatureResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @GetMapping("/features/{id}")
    fun getCarFeature(@PathVariable("id") id: Long): ResponseEntity<CarFeatureResDTO> {
        require(id > 0) { "Invalid car feature id $id: it must be a positive number" }
        return ResponseEntity.ok(carModelService.getCarFeatureById(id))
    }

    @Operation(
        summary = "Get car features by car model id",
        description = "Retrieves a list of car features having the specified car model id or throws an exception if the car model is not found",
        responses = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(
                        schema = Schema(implementation = CarFeatureResDTO::class)
                    )
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
        ]
    )
    @GetMapping("{id}/features")
    fun getCarModelFeatures(@PathVariable("id") id: Long): ResponseEntity<List<CarFeatureResDTO>> {
        require(id > 0) { "Invalid car model id $id: it must be a positive number" }
        return ResponseEntity.ok(carModelService.getCarFeaturesByCarModelId(id))
    }

    @Operation(
        summary = "Add car model",
        description = "Creates a new car model ",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = CarModelReqDTO::class)
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "201", content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CarModel::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "409", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @PostMapping("")
    fun createCarModel(
        @RequestBody model: CarModelReqDTO,
        uriBuilder: UriComponentsBuilder
    ): ResponseEntity<CarModelResDTO> {
        val createdModel = carModelService.createCarModel(model)
        logger.info("Created car model: {}", mapper.writeValueAsString(createdModel))
        val location = uriBuilder
            .path("/api/v1/models/{id}")
            .buildAndExpand(createdModel.id)
            .toUri()
        return ResponseEntity
            .created(location) // sets status 201 and Location header
            .body(createdModel)
    }

    @Operation(
        summary = "Update car model by id",
        description = "Updates a car model having the specified id or throws an exception if the car model is not found",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CarModelResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @PutMapping("{id}")
    fun updateCarModel(
        @PathVariable id: Long,
        @RequestBody model: CarModelReqDTO
    ): ResponseEntity<CarModelResDTO> {
        require(id > 0) { "Invalid car model id $id: it must be a positive number" }
        val updatedModel = carModelService.updateCarModel(id, model)
        logger.info("Updated car model {}: {}", id, mapper.writeValueAsString(updatedModel))
        return ResponseEntity.ok(updatedModel)
    }

    @Operation(
        summary = "Delete car model by id",
        description = "Deletes a car model having the specified id or throws an exception if the car model is not found",
        responses = [
            ApiResponse(responseCode = "204", content = [Content()]),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @DeleteMapping("{id}")
    fun deleteCarModel(@PathVariable("id") id: Long): ResponseEntity<Unit> {
        require(id > 0) { "Invalid car model id $id: it must be a positive number" }
        carModelService.deleteCarModelById(id)
        logger.info("Deleted car model {}", id)
        return ResponseEntity.noContent().build()
    }
}
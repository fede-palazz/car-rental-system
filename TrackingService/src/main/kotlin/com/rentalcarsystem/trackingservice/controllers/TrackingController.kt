package com.rentalcarsystem.trackingservice.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.rentalcarsystem.trackingservice.services.TrackingService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val NOTE_BASE_URL = "/api/v1/tracking"

@RestController
@RequestMapping(NOTE_BASE_URL)
@CrossOrigin(origins = ["http://localhost:8083"], allowCredentials = "true")
class TrackingController(private val trackingService: TrackingService) {

    private val logger = LoggerFactory.getLogger(TrackingController::class.java)
    private val mapper = ObjectMapper().apply { registerModule(JavaTimeModule()) }

//    @Operation(
//        summary = "Get vehicle notes",
//        description = "Retrieves all notes related to a vehicle based on the specified query parameters",
//        responses = [
//            ApiResponse(
//                responseCode = "200", content = [Content(
//                    mediaType = "application/json",
//                    schema = Schema(implementation = PagedResDTO::class)
//                )]
//            ),
//            ApiResponse(responseCode = "400", content = [Content()]),
//            ApiResponse(responseCode = "404", content = [Content()]),
//            ApiResponse(responseCode = "422", content = [Content()]),
//        ]
//    )
//    @GetMapping("")
//    fun getVehicleNotes(
//        @RequestParam("page", defaultValue = "0") page: Int,
//        @RequestParam("size", defaultValue = "10") size: Int,
//        @RequestParam("sort", defaultValue = "date") sortBy: String,
//        @RequestParam("order", defaultValue = "desc") sortOrder: String,
//        @PathVariable vehicleId: Long,
//        @ModelAttribute filters: NoteFilter
//    ): ResponseEntity<PagedResDTO<NoteResDTO>> {
//        if (vehicleId <= 0) {
//            throw IllegalArgumentException("Vehicle id must be a positive number")
//        }
//        // Validate filters
//        if (page < 0) {
//            throw IllegalArgumentException("Parameter 'page' must be greater than or equal to zero")
//        }
//        if (size < 1) {
//            throw IllegalArgumentException("Parameter 'size' must be greater than zero")
//        }
//        // Check if minDate is after maxDate
//        if (filters.minDate != null && filters.maxDate != null && filters.minDate.isAfter(filters.maxDate)) {
//            throw IllegalArgumentException("Parameter 'minDate' must be before or equal to 'maxDate'")
//        }
//        // Retrieve NodeFilter fields' names
//        val allowedSortFields = listOf(
//            "content",
//            "author",
//            "date"
//        )
//        if (sortBy !in allowedSortFields) {
//            throw IllegalArgumentException("Parameter 'sort' invalid. Allowed values: $allowedSortFields")
//        }
//        if (sortOrder !in listOf("asc", "desc")) {
//            throw IllegalArgumentException("Parameter 'sortOrder' invalid. Allowed values: ['asc', 'desc']")
//        }
//        return ResponseEntity.ok(
//            trackingService.getNotes(
//                page, size, sortBy, sortOrder, vehicleId, filters
//            )
//        )
//    }

}
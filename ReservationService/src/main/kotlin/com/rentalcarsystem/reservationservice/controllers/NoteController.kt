package com.rentalcarsystem.reservationservice.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.rentalcarsystem.reservationservice.dtos.request.NoteReqDTO
import com.rentalcarsystem.reservationservice.dtos.response.NoteResDTO
import com.rentalcarsystem.reservationservice.dtos.response.PagedResDTO
import com.rentalcarsystem.reservationservice.exceptions.FailureException
import com.rentalcarsystem.reservationservice.exceptions.ResponseEnum
import com.rentalcarsystem.reservationservice.filters.NoteFilter
import com.rentalcarsystem.reservationservice.services.NoteService
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

const val NOTE_BASE_URL = "/api/v1/vehicles/{vehicleId}/notes"

@RestController
@RequestMapping(NOTE_BASE_URL)
@CrossOrigin(origins = ["http://localhost:8083"], allowCredentials = "true")
class NoteController(private val noteService: NoteService) {
    private val logger = LoggerFactory.getLogger(MaintenanceController::class.java)
    private val mapper = ObjectMapper().apply { registerModule(JavaTimeModule()) }

    @Operation(
        summary = "Get vehicle notes",
        description = "Retrieves all notes related to a vehicle based on the specified query parameters",
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
    @GetMapping("")
    fun getVehicleNotes(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
        @RequestParam("sort", defaultValue = "date") sortBy: String,
        @RequestParam("order", defaultValue = "desc") sortOrder: String,
        @PathVariable vehicleId: Long,
        @ModelAttribute filters: NoteFilter
    ): ResponseEntity<PagedResDTO<NoteResDTO>> {
        if (vehicleId <= 0) {
            throw IllegalArgumentException("Vehicle id must be a positive number")
        }
        // Validate filters
        if (page < 0) {
            throw IllegalArgumentException("Parameter 'page' must be greater than or equal to zero")
        }
        if (size < 1) {
            throw IllegalArgumentException("Parameter 'size' must be greater than zero")
        }
        // Check if minDate is after maxDate
        if (filters.minDate != null && filters.maxDate != null && filters.minDate.isAfter(filters.maxDate)) {
            throw IllegalArgumentException("Parameter 'minDate' must be before or equal to 'maxDate'")
        }
        // Retrieve NodeFilter fields' names
        val allowedSortFields = listOf(
            "content",
            "author",
            "date"
        )
        if (sortBy !in allowedSortFields) {
            throw IllegalArgumentException("Parameter 'sort' invalid. Allowed values: $allowedSortFields")
        }
        if (sortOrder !in listOf("asc", "desc")) {
            throw IllegalArgumentException("Parameter 'sortOrder' invalid. Allowed values: ['asc', 'desc']")
        }
        return ResponseEntity.ok(
            noteService.getNotes(
                page, size, sortBy, sortOrder, vehicleId, filters
            )
        )
    }

    @Operation(
        summary = "Add vehicle's note",
        description = "Creates a new note for the specified vehicle ",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = NoteReqDTO::class)
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "201", content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = NoteResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "409", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @PostMapping("")
    fun createNote(
        @PathVariable vehicleId: Long,
        @RequestBody note: NoteReqDTO,
        uriBuilder: UriComponentsBuilder
    ): ResponseEntity<NoteResDTO> {
        if (vehicleId <= 0) {
            throw IllegalArgumentException("Vehicle id must be a positive number")
        }
        val authentication = SecurityContextHolder.getContext().authentication

        // Extract username
        val jwt = authentication.principal as Jwt
        val username = jwt.getClaimAsString("preferred_username")
        requireNotNull(username) { FailureException(ResponseEnum.FORBIDDEN) }

        val createdNote = noteService.createNote(vehicleId, note, username)
        logger.info("Created note for vehicle {}: {}", vehicleId, mapper.writeValueAsString(createdNote))
        val location = uriBuilder
            .path("$NOTE_BASE_URL/${createdNote.id}")
            .buildAndExpand(createdNote.id)
            .toUri()
        return ResponseEntity
            .created(location) // sets status 201 and Location header
            .body(createdNote)
    }

    @Operation(
        summary = "Update note by id",
        description = "Updates a note having the specified id or throws an exception if the note is not found",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = NoteResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @PutMapping("{id}")
    fun updateNote(
        @PathVariable id: Long,
        @PathVariable vehicleId: Long,
        @RequestBody note: NoteReqDTO
    ): ResponseEntity<NoteResDTO> {
        if (vehicleId <= 0) {
            throw IllegalArgumentException("Invalid vehicle id $vehicleId: it must be a positive number")
        }
        if (id <= 0) {
            throw IllegalArgumentException("Invalid note id $id: it must be a positive number")
        }
        val updatedNote = noteService.updateNote(id, vehicleId, note)
        logger.info("Updated note {} for vehicle {}: {}", id, vehicleId, mapper.writeValueAsString(updatedNote))
        return ResponseEntity.ok(updatedNote)
    }

    @Operation(
        summary = "Delete note by id",
        description = "Deletes a note having the specified id or throws an exception if the note is not found",
        responses = [
            ApiResponse(responseCode = "204", content = [Content()]),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @DeleteMapping("{id}")
    fun deleteNote(
        @PathVariable vehicleId: Long,
        @PathVariable("id") noteId: Long,
    ): ResponseEntity<Unit> {
        if (vehicleId <= 0) {
            throw IllegalArgumentException("Invalid vehicle id $vehicleId: it must be a positive number")
        }
        if (noteId <= 0) {
            throw IllegalArgumentException("Invalid note id $noteId: it must be a positive number")
        }
        noteService.deleteNote(vehicleId, noteId)
        logger.info("Deleted note {} for vehicle {}", noteId, vehicleId)
        return ResponseEntity.noContent().build()
    }
}
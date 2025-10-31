package com.rentalcarsystem.usermanagementservice.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.rentalcarsystem.usermanagementservice.dtos.request.UserReqDTO
import com.rentalcarsystem.usermanagementservice.dtos.request.UserUpdateReqDTO
import com.rentalcarsystem.usermanagementservice.dtos.response.UserResDTO
import com.rentalcarsystem.usermanagementservice.filters.UserFilter
import com.rentalcarsystem.usermanagementservice.services.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = ["http://localhost:8083"], allowCredentials = "true")
class UserController(private val userService: UserService) {
    private val logger = LoggerFactory.getLogger(UserController::class.java)
    private val mapper = ObjectMapper().apply { registerModule(JavaTimeModule()) }

    @Operation(
        summary = "Get all users",
        description = "Retrieves all users based on the specified query parameters",
        responses = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(
                        schema = Schema(implementation = UserResDTO::class)
                    )
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @PreAuthorize("hasAnyRole('STAFF', 'FLEET_MANAGER', 'MANAGER')")
    @GetMapping
    fun getUsers(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
        @RequestParam("sort", defaultValue = "lastName") sortBy: String,
        @RequestParam("order", defaultValue = "asc") sortOrder: String,
        @ModelAttribute filters: UserFilter
    ): ResponseEntity<List<UserResDTO>> {
        return ResponseEntity.ok(userService.getUsers(page, size, sortBy, sortOrder, filters))
    }

    @Operation(
        summary = "Get user by id",
        description = "Retrieves a user based on the specified id or throws an exception if the user does not exist",
        responses = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(
                        schema = Schema(implementation = UserResDTO::class)
                    )
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @GetMapping("/{userId}")
    fun getUserById(@PathVariable userId: Long): ResponseEntity<UserResDTO> {
        require(userId > 0) { "Invalid user id $userId: it must be a positive number" }
        return ResponseEntity.ok(userService.getUserById(userId))
    }

    @Operation(
        summary = "Get user by username",
        description = "Retrieves a user based on the specified username or throws an exception if the user does not exist",
        responses = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(
                        schema = Schema(implementation = UserResDTO::class)
                    )
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @GetMapping("/username/{username}")
    fun getUserByUsername(
        @PathVariable username: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<UserResDTO> {
        require(username.isNotBlank()) { "Invalid username $username: it must be a positive number" }
        val loggedUsername = jwt.claims["preferred_username"] as String


        @Suppress("UNCHECKED_CAST")
        val realmAccess = jwt.claims["realm_access"] as? Map<String, Any> ?: emptyMap()
        val roles = (realmAccess["roles"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

        return ResponseEntity.ok(
            userService.getUserByUsername(
                username,
                if (loggedUsername == "service-account-reservation-service") username else loggedUsername,
                roles
            )
        )
    }

    @Operation(
        summary = "Add user",
        description = "Create a new user",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UserReqDTO::class)
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "201", content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UserResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "409", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @PostMapping
    fun addUser(
        @RequestBody userDTO: UserReqDTO,
        uriBuilder: UriComponentsBuilder
    ): ResponseEntity<UserResDTO> {
        val created = userService.addUser(userDTO)
        val location = uriBuilder.path("/api/v1/users/{id}")
            .buildAndExpand(created.id).toUri()
        logger.info("Created user: {}", mapper.writeValueAsString(created))
        return ResponseEntity.created(location).body(created)
    }

    @Operation(
        summary = "Update user by id",
        description = "Updates a user data having the specified id or throws an exception if the user does not exist",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UserResDTO::class)
                )]
            ),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @PutMapping("/{userId}")
    fun updateUser(
        @PathVariable userId: Long,
        @RequestBody updateDTO: UserUpdateReqDTO
    ): ResponseEntity<UserResDTO> {
        val updated = userService.updateUser(userId, updateDTO)
        logger.info("Updated user $userId: {}", mapper.writeValueAsString(updated))
        return ResponseEntity.ok(updated)
    }

    @Operation(
        summary = "Delete user by id",
        description = "Deletes a user having the specified id or throws an exception if the user does not exist",
        responses = [
            ApiResponse(responseCode = "204", content = [Content()]),
            ApiResponse(responseCode = "400", content = [Content()]),
            ApiResponse(responseCode = "404", content = [Content()]),
            ApiResponse(responseCode = "422", content = [Content()]),
        ]
    )
    @DeleteMapping("/{userId}")
    fun deleteUser(@PathVariable userId: Long): ResponseEntity<Unit> {
        userService.deleteUser(userId)
        logger.info("Deleted user $userId")
        return ResponseEntity.noContent().build()
    }
}

package com.rentalcarsystem.usermanagementservice.filters

import com.rentalcarsystem.usermanagementservice.models.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class UserFilter(
    @field:Size(min = 1, max = 50, message = "Parameter 'username' must be between 1 and 50 characters")
    val username: String? = null,

    @field:Size(min = 1, max = 50, message = "Parameter 'firstName' must be between 1 and 50 characters")
    val firstName: String? = null,

    @field:Size(min = 1, max = 50, message = "Parameter 'lastName' must be between 1 and 50 characters")
    val lastName: String? = null,

    @field:Email(message = "Parameter 'email' must be a valid email")
    @field:Size(max = 100, message = "Parameter 'email' must be less than 100 characters")
    val email: String? = null,

    @field:Size(min = 1, max = 15, message = "Parameter 'phone' must be between 1 and 15 characters")
    val phone: String? = null,

    val role: UserRole? = null,
)

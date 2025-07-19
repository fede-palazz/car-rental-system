package com.rentalcarsystem.usermanagementservice.dtos.request

import com.rentalcarsystem.usermanagementservice.models.UserRole
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class UserUpdateReqDTO(
    @field:NotBlank(message = "First name must not be blank")
    val firstName: String,
    @field:NotBlank(message = "Last name must not be blank")
    val lastName: String,
    @field:NotNull(message = "Category must not be blank")
    val role: UserRole,
    @field:NotBlank(message = "Phone number must not be blank")
    @field:Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 digits")
    val phone: String,
    @field:NotBlank(message = "Address must not be blank")
    val address: String,
    val eligibilityScore: Double?
)

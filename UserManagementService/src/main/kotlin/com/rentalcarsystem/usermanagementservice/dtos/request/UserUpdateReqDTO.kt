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

    @field:NotBlank(message = "Phone number must not be blank")
    val phone: String,

    @field:NotBlank(message = "Address must not be blank")
    val address: String,

    val role: UserRole?,
    val eligibilityScore: Double?
)

package com.rentalcarsystem.usermanagementservice.dtos.request

import com.rentalcarsystem.usermanagementservice.models.User
import com.rentalcarsystem.usermanagementservice.models.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

const val INITIAL_ELIGIBILITY_SCORE = 100.0

data class UserReqDTO(
    @field:NotBlank(message = "Username must not be blank")
    val username: String,

    @field:NotBlank(message = "First name must not be blank")
    val firstName: String,

    @field:NotBlank(message = "Last name must not be blank")
    val lastName: String,

    @field:Email(message = "Email must be valid")
    val email: String,

    @field:NotBlank(message = "Phone number must not be blank")
    val phone: String,

    @field:NotBlank(message = "Address must not be blank")
    val address: String,

    @field:NotNull(message = "Role must not be null")
    val role: UserRole,
)

fun UserReqDTO.toEntity() = User(
    username,
    firstName,
    lastName,
    email,
    role,
    phone,
    address,
    if (role == UserRole.CUSTOMER) INITIAL_ELIGIBILITY_SCORE else 0.0
)
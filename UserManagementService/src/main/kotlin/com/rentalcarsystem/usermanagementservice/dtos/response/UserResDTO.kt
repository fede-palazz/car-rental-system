package com.rentalcarsystem.usermanagementservice.dtos.response

import com.rentalcarsystem.usermanagementservice.models.User
import com.rentalcarsystem.usermanagementservice.models.UserRole

data class UserResDTO(
    val id: Long,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val eligibilityScore: Double?,
    val role: UserRole,
    val phone: String,
    val address: String,
)

fun User.toResDTO() = UserResDTO(
    this.getId()!!,
    username,
    firstName,
    lastName,
    email,
    eligibilityScore,
    role,
    phone,
    address
)
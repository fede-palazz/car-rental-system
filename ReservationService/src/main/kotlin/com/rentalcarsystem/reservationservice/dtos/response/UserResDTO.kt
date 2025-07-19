package com.rentalcarsystem.reservationservice.dtos.response

import com.rentalcarsystem.reservationservice.enums.UserRole

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

package com.rentalcarsystem.reservationservice.dtos.request

import com.rentalcarsystem.reservationservice.enums.UserRole

data class UserUpdateReqDTO(
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val phone: String,
    val address: String,
    val eligibilityScore: Double,
)
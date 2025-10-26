package com.rentalcarsystem.analyticsservice.dtos.response

data class ReservationLevelCountResDTO(
    val levelZeroCount: Int,
    val levelOneCount: Int,
    val levelTwoCount: Int,
    val levelThreeCount: Int,
    val levelFourCount: Int,
    val levelFiveCount: Int
)
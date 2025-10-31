package com.rentalcarsystem.reservationservice.dtos.request

import java.time.LocalDateTime

data class FinalizeMaintenanceReqDTO(
    val actualEndDate: LocalDateTime
)

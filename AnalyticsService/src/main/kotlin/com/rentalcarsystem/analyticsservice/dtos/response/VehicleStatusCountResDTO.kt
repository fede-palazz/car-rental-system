package com.rentalcarsystem.analyticsservice.dtos.response

data class VehicleStatusCountResDTO(
    val availableCount: Int,
    val rentedCount: Int,
    val inMaintenanceCount: Int
)
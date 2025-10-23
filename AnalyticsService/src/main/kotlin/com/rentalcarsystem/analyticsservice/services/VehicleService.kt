package com.rentalcarsystem.analyticsservice.services

import com.rentalcarsystem.analyticsservice.dtos.response.VehicleStatusCountResDTO
import java.time.LocalDate

interface VehicleService {
    fun getVehicleStatusCount(
        desiredDate: LocalDate
    ): VehicleStatusCountResDTO
}
package com.rentalcarsystem.analyticsservice.services

import com.rentalcarsystem.analyticsservice.dtos.response.VehicleKmTravelledResDTO
import com.rentalcarsystem.analyticsservice.dtos.response.VehicleStatusCountResDTO
import com.rentalcarsystem.analyticsservice.enums.Granularity
import java.time.LocalDate

interface VehicleService {
    fun getVehicleStatusCount(
        desiredDate: LocalDate
    ): VehicleStatusCountResDTO

    fun getVehicleKmTravelled(
        vin: String,
        desiredStart: LocalDate,
        desiredEnd: LocalDate,
        granularity: Granularity,
        average: Boolean
    ): List<VehicleKmTravelledResDTO>
}
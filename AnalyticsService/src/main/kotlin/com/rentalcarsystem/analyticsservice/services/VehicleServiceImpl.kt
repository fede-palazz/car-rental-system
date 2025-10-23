package com.rentalcarsystem.analyticsservice.services

import com.rentalcarsystem.analyticsservice.dtos.response.VehicleStatusCountResDTO
import com.rentalcarsystem.analyticsservice.repositories.VehicleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import java.time.LocalDate

@Service
@Transactional
@Validated
class VehicleServiceImpl(
    private val vehicleRepository: VehicleRepository,
) : VehicleService {
    override fun getVehicleStatusCount(desiredDate: LocalDate): VehicleStatusCountResDTO {
        val result = vehicleRepository.getVehicleStatusCountByEntryDate(desiredDate).firstOrNull()
        if (result == null) {
            return VehicleStatusCountResDTO(0, 0, 0) // no rows for that date -> all zeros
        }
        return VehicleStatusCountResDTO(
            availableCount = (result[0] as Number).toInt(),
            rentedCount = (result[1] as Number).toInt(),
            inMaintenanceCount = (result[2] as Number).toInt()
        )
    }
}

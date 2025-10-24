package com.rentalcarsystem.analyticsservice.services

import com.rentalcarsystem.analyticsservice.dtos.response.VehicleKmTravelledResDTO
import com.rentalcarsystem.analyticsservice.dtos.response.VehicleStatusCountResDTO
import com.rentalcarsystem.analyticsservice.enums.Granularity
import com.rentalcarsystem.analyticsservice.exceptions.FailureException
import com.rentalcarsystem.analyticsservice.exceptions.ResponseEnum
import com.rentalcarsystem.analyticsservice.repositories.VehicleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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

    override fun getVehicleKmTravelled(
        vin: String,
        desiredStart: LocalDate,
        desiredEnd: LocalDate,
        granularity: Granularity,
        average: Boolean
    ): List<VehicleKmTravelledResDTO> {
        if (!vehicleRepository.existsByVin(vin)) {
            throw FailureException(ResponseEnum.VEHICLE_NOT_FOUND, "Vehicle with vin $vin not found!")
        }
        // DB returns only non-zero elements (grouped by DATE_TRUNC)
        val result = vehicleRepository.getVehicleKmTravelledByVinAndEntryDateAndGranularity(
            vin, desiredStart, desiredEnd, Granularity.getValue(granularity), average
        )

        // map query result to a map with elementStart (LocalDate) -> vehicleKmTravelled (Double)
        val kmTravelledMap: Map<LocalDate, Double> = result.associate { arr ->
            val elementStart = (arr[0] as Instant).atZone(ZoneId.of("Europe/Rome")).toLocalDate()
            val vehicleKmTravelled = (arr[1] as Number).toDouble()   // arr[1]?.let { it -> (it as Number).toDouble() } ?: 0.0
            elementStart to vehicleKmTravelled
        }

        // compute the full sequence of truncated elements starts from truncated(minDate) to truncated(maxDate)
        val elements = mutableListOf<VehicleKmTravelledResDTO>()
        var cursor = truncateToElementStart(desiredStart, granularity)      // e.g. 2025-04-01T00:00
        val lastElement = truncateToElementStart(desiredEnd, granularity)   // e.g. 2025-06-01T00:00

        while (!cursor.isAfter(lastElement)) {
            val amount = kmTravelledMap[cursor] ?: 0.0
            elements.add(VehicleKmTravelledResDTO(elementStart = cursor, vehicleKmTravelled = amount))
            cursor = nextElementStart(cursor, granularity)
        }

        return elements
    }

    /*************************************************************************************************************/

    private fun truncateToElementStart(dt: LocalDate, g: Granularity): LocalDate {
        return when (g) {
            Granularity.DAY -> dt
            Granularity.MONTH -> dt.withDayOfMonth(1)
            Granularity.YEAR -> dt.withDayOfYear(1)
        }
    }

    private fun nextElementStart(dt: LocalDate, g: Granularity): LocalDate {
        return when (g) {
            Granularity.DAY -> dt.plusDays(1)
            Granularity.MONTH -> dt.plusMonths(1)
            Granularity.YEAR -> dt.plusYears(1)
        }
    }
}

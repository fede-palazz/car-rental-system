package com.rentalcarsystem.analyticsservice.services

import com.rentalcarsystem.analyticsservice.dtos.response.ReservationsCountResDTO
import com.rentalcarsystem.analyticsservice.dtos.response.ReservationsTotalAmountResDTO
import com.rentalcarsystem.analyticsservice.enums.Granularity
import com.rentalcarsystem.analyticsservice.repositories.ReservationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import java.sql.Timestamp
import java.time.LocalDateTime

@Service
@Transactional
@Validated
class ReservationServiceImpl(
    private val reservationRepository: ReservationRepository,
) : ReservationService {
    override fun getReservationsCount(
        desiredStart: LocalDateTime,
        desiredEnd: LocalDateTime,
        granularity: Granularity
    ): List<ReservationsCountResDTO> {
        // DB returns only non-zero elements (grouped by DATE_TRUNC)
        val result = reservationRepository.getReservationsCountByCreationDateAndGranularity(
            desiredStart, desiredEnd, Granularity.getValue(granularity)
        )

        // map query result to a map with elementStart (LocalDateTime) -> reservationsCount (Int)
        val countMap: Map<LocalDateTime, Int> = result.associate { arr ->
            val elementStart = (arr[0] as Timestamp).toLocalDateTime()
            val reservationsCount = (arr[1] as Number).toInt()
            elementStart to reservationsCount
        }

        // compute the full sequence of truncated elements starts from truncated(minDate) to truncated(maxDate)
        val elements = mutableListOf<ReservationsCountResDTO>()
        var cursor = truncateToElementStart(desiredStart, granularity)      // e.g. 2025-04-01T00:00
        val lastElement = truncateToElementStart(desiredEnd, granularity)   // e.g. 2025-06-01T00:00

        while (!cursor.isAfter(lastElement)) {
            val count = countMap[cursor] ?: 0
            elements.add(ReservationsCountResDTO(elementStart = cursor, reservationsCount = count))
            cursor = nextElementStart(cursor, granularity)
        }

        return elements
    }

    override fun getReservationsTotalAmount(
        desiredStart: LocalDateTime,
        desiredEnd: LocalDateTime,
        granularity: Granularity,
        average: Boolean
    ): List<ReservationsTotalAmountResDTO> {
        // DB returns only non-zero elements (grouped by DATE_TRUNC)
        val result = reservationRepository.getReservationsTotalAmountByCreationDateAndGranularity(
            desiredStart, desiredEnd, Granularity.getValue(granularity), average
        )

        // map query result to a map with elementStart (LocalDateTime) -> reservationsTotalAmount (Double)
        val totalAmountMap: Map<LocalDateTime, Double> = result.associate { arr ->
            val elementStart = (arr[0] as Timestamp).toLocalDateTime()
            val reservationsTotalAmount = (arr[1] as Number).toDouble()   // arr[1]?.let { it -> (it as Number).toDouble() } ?: 0.0
            elementStart to reservationsTotalAmount
        }

        // compute the full sequence of truncated elements starts from truncated(minDate) to truncated(maxDate)
        val elements = mutableListOf<ReservationsTotalAmountResDTO>()
        var cursor = truncateToElementStart(desiredStart, granularity)      // e.g. 2025-04-01T00:00
        val lastElement = truncateToElementStart(desiredEnd, granularity)   // e.g. 2025-06-01T00:00

        while (!cursor.isAfter(lastElement)) {
            val amount = totalAmountMap[cursor] ?: 0.0
            elements.add(ReservationsTotalAmountResDTO(elementStart = cursor, reservationsTotalAmount = amount))
            cursor = nextElementStart(cursor, granularity)
        }

        return elements
    }

    /*************************************************************************************************************/

    private fun truncateToElementStart(dt: LocalDateTime, g: Granularity): LocalDateTime {
        return when (g) {
            Granularity.DAY -> dt.toLocalDate().atStartOfDay()
            Granularity.MONTH -> dt.withDayOfMonth(1).toLocalDate().atStartOfDay()
            Granularity.YEAR -> dt.withDayOfYear(1).toLocalDate().atStartOfDay()
        }
    }

    private fun nextElementStart(dt: LocalDateTime, g: Granularity): LocalDateTime {
        return when (g) {
            Granularity.DAY -> dt.plusDays(1)
            Granularity.MONTH -> dt.plusMonths(1)
            Granularity.YEAR -> dt.plusYears(1)
        }
    }
}

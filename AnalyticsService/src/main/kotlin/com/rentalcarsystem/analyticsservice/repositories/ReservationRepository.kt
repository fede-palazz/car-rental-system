package com.rentalcarsystem.analyticsservice.repositories

import com.rentalcarsystem.analyticsservice.models.Reservation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ReservationRepository : JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {
    @Query(
        value = """
        SELECT date_trunc(:granularity, r.creation_date) AS element_start,
            count(*) AS cnt
        FROM reservations r
        WHERE r.creation_date BETWEEN :minDate AND :maxDate
        GROUP BY element_start
        ORDER BY element_start
        """,
        nativeQuery = true
    )
    fun getReservationsCountByCreationDateAndGranularity(
        @Param("minDate") minDate: LocalDateTime,
        @Param("maxDate") maxDate: LocalDateTime,
        @Param("granularity") granularity: String // "day" | "month" | "year"
    ): List<Array<Any>>

    @Query(
        value = """
        SELECT date_trunc(:granularity, r.creation_date) AS element_start,
            CASE WHEN :average THEN AVG(r.total_amount) ELSE SUM(r.total_amount) END AS value
        FROM reservations r
        WHERE r.creation_date BETWEEN :minDate AND :maxDate
        AND (r.status = 'CONFIRMED' OR r.status = 'PICKED_UP' OR r.status = 'DELIVERED')
        GROUP BY element_start
        ORDER BY element_start
        """,
        nativeQuery = true
    )
    fun getReservationsTotalAmountByCreationDateAndGranularity(
        @Param("minDate") minDate: LocalDateTime,
        @Param("maxDate") maxDate: LocalDateTime,
        @Param("granularity") granularity: String,
        @Param("average") average: Boolean
    ): List<Array<Any>>
}
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
        SELECT DATE_TRUNC(:granularity, r.creation_date) AS element_start,
            COUNT(*) AS cnt
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
        SELECT DATE_TRUNC(:granularity, r.creation_date) AS element_start,
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

    @Query(
        value = """
        SELECT 
            COUNT(*) FILTER (WHERE CASE WHEN :dirtiness THEN r.dirtiness_level = 0 ELSE r.damage_level = 0 END) AS levelZeroCount,
            COUNT(*) FILTER (WHERE CASE WHEN :dirtiness THEN r.dirtiness_level = 1 ELSE r.damage_level = 1 END) AS levelOneCount,
            COUNT(*) FILTER (WHERE CASE WHEN :dirtiness THEN r.dirtiness_level = 2 ELSE r.damage_level = 2 END) AS levelTwoCount,
            COUNT(*) FILTER (WHERE CASE WHEN :dirtiness THEN r.dirtiness_level = 3 ELSE r.damage_level = 3 END) AS levelThreeCount,
            COUNT(*) FILTER (WHERE CASE WHEN :dirtiness THEN r.dirtiness_level = 4 ELSE r.damage_level = 4 END) AS levelFourCount,
            COUNT(*) FILTER (WHERE CASE WHEN :dirtiness THEN r.dirtiness_level = 5 ELSE r.damage_level = 5 END) AS levelFiveCount
        FROM reservations r
        WHERE r.actual_drop_off_date BETWEEN :minDate AND :maxDate;
        """,
        nativeQuery = true
    )
    fun getReservationLevelCountByActualDropOffDate(
        @Param("minDate") minDate: LocalDateTime,
        @Param("maxDate") maxDate: LocalDateTime,
        @Param("dirtiness") dirtiness: Boolean
    ): List<Array<Any>>
}
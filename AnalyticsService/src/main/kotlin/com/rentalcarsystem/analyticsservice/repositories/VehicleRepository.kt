package com.rentalcarsystem.analyticsservice.repositories

import com.rentalcarsystem.analyticsservice.models.Vehicle
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface VehicleRepository : JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {
    fun existsByVin(vin: String): Boolean

    @Query(
        value = """
        SELECT 
            COUNT(*) FILTER (WHERE v.status = 'AVAILABLE') AS available_count,
            COUNT(*) FILTER (WHERE v.status = 'RENTED') AS rented_count,
            COUNT(*) FILTER (WHERE v.status = 'IN_MAINTENANCE') AS in_maintenance_count
        FROM vehicles v
        WHERE v.entry_date = :desiredDate;
        """,
        nativeQuery = true
    )
    fun getVehicleStatusCountByEntryDate(
        @Param("desiredDate") desiredDate: LocalDate
    ): List<Array<Any>>

    @Query(
        value = """
        SELECT DATE_TRUNC(:granularity, v.entry_date) AS element_start,
            CASE WHEN :average THEN AVG(v.km_travelled) ELSE SUM(v.km_travelled) END AS value
        FROM vehicles v
        WHERE v.entry_date BETWEEN :minDate AND :maxDate
        AND v.vin = :vin
        AND v.km_travelled > 0
        GROUP BY element_start
        ORDER BY element_start
        """,
        nativeQuery = true
    )
    fun getVehicleKmTravelledByVinAndEntryDateAndGranularity(
        @Param("vin") vin: String,
        @Param("minDate") minDate: LocalDate,
        @Param("maxDate") maxDate: LocalDate,
        @Param("granularity") granularity: String,
        @Param("average") average: Boolean
    ): List<Array<Any>>

}
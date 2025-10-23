package com.rentalcarsystem.analyticsservice.repositories

import com.rentalcarsystem.analyticsservice.models.Vehicle
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface VehicleRepository : JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {
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

}
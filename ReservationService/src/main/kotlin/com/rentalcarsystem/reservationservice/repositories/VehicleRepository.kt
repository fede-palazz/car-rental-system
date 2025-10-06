package com.rentalcarsystem.reservationservice.repositories

import com.rentalcarsystem.reservationservice.models.CarModel
import com.rentalcarsystem.reservationservice.models.Vehicle
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface VehicleRepository : JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {
    fun existsByLicensePlate(licensePlate: String): Boolean
    fun existsByVin(vin: String): Boolean
    fun deleteAllByCarModelId(carModelId: Long)

    // When fetching the paginated vehicles, also fetch the carModel info
    // to avoid the N+1 problem
    @EntityGraph(attributePaths = ["carModel"])
    override fun findAll(spec: Specification<Vehicle>?, pageable: Pageable): Page<Vehicle>
    fun getVehicleByLicensePlate(licensePlate: String): MutableList<Vehicle>

    @Query(
        """
        SELECT v FROM Vehicle v
        WHERE v.carModel = :carModel
        AND NOT EXISTS (
            SELECT r FROM Reservation r
            WHERE r.vehicle = v
            AND r.plannedPickUpDate < :desiredEndWithBuffer
            AND r.plannedDropOffDate > :desiredStartWithBuffer
        )
        AND NOT EXISTS (
            SELECT m FROM Maintenance m
            WHERE m.vehicle = v
            AND m.startDate <= :desiredEnd
            AND m.plannedEndDate >= :desiredStart
        )
        """
    )
    fun findAvailableVehiclesByModelAndDateRange(
        @Param("carModel") carModel: CarModel,
        @Param("desiredStartWithBuffer") desiredStartWithBuffer: LocalDateTime,
        @Param("desiredEndWithBuffer") desiredEndWithBuffer: LocalDateTime,
        @Param("desiredStart") desiredStart: LocalDateTime,
        @Param("desiredEnd") desiredEnd: LocalDateTime,
        pageable: Pageable
    ): Page<Vehicle>

    @Query(
        """
        SELECT v FROM Vehicle v
        WHERE v.id = :vehicleId
        AND v.carModel = (
            SELECT r.vehicle.carModel FROM Reservation r
            WHERE r.id = :reservationToExcludeId
        )
        AND NOT EXISTS (
            SELECT r FROM Reservation r
            WHERE r.vehicle = v
            AND r.id <> :reservationToExcludeId
            AND r.plannedPickUpDate < :desiredEndWithBuffer
            AND r.plannedDropOffDate > :desiredStartWithBuffer
        )
        AND NOT EXISTS (
            SELECT m FROM Maintenance m
            WHERE m.vehicle = v
            AND m.startDate <= :desiredEnd
            AND m.plannedEndDate >= :desiredStart
        )
        """
    )
    fun findAvailableVehicleByIdAndDateRange(
        @Param("vehicleId") vehicleId: Long,
        @Param("reservationToExcludeId") reservationToExcludeId: Long,
        @Param("desiredStartWithBuffer") desiredStartWithBuffer: LocalDateTime,
        @Param("desiredEndWithBuffer") desiredEndWithBuffer: LocalDateTime,
        @Param("desiredStart") desiredStart: LocalDateTime,
        @Param("desiredEnd") desiredEnd: LocalDateTime
    ): Vehicle?
}
package com.rentalcarsystem.reservationservice.repositories

import com.rentalcarsystem.reservationservice.enums.ReservationStatus
import com.rentalcarsystem.reservationservice.models.CarModel
import com.rentalcarsystem.reservationservice.models.Reservation
import com.rentalcarsystem.reservationservice.models.Vehicle
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ReservationRepository : JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {
    fun findAllByCustomerUsername(customerUsername: String): List<Reservation>

    @Query(
        """
        SELECT COUNT(r) FROM Reservation r 
        WHERE r.vehicle.id = :vehicleId 
        AND r.plannedPickUpDate <= :endDate
        AND r.plannedDropOffDate >= :startDate
        """
    )
    fun countVehicleReservationsBetweenDates(
        @Param("vehicleId") vehicleId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Int

    @Query(
        """
        SELECT v FROM Vehicle v
        WHERE v.carModel = :carModel
        AND v.status <> 'IN_MAINTENANCE'
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
        ORDER BY v.id ASC
        """
    )
    fun findFirstAvailableVehicleByModelAndDateRange(
        @Param("carModel") carModel: CarModel,
        @Param("desiredStartWithBuffer") desiredStartWithBuffer: LocalDateTime,
        @Param("desiredEndWithBuffer") desiredEndWithBuffer: LocalDateTime,
        @Param("desiredStart") desiredStart: LocalDateTime,
        @Param("desiredEnd") desiredEnd: LocalDateTime,
        pageable: Pageable = PageRequest.of(0, 1)
    ): List<Vehicle>

    fun existsByCustomerUsernameAndStatus(customerUsername: String, status: ReservationStatus): Boolean

    @Query(
        """
        SELECT r FROM Reservation r 
        WHERE r.status = :activeStatus 
        AND r.creationDate <= :expiryThreshold
    """
    )
    fun findExpiredReservations(
        @Param("activeStatus") activeStatus: ReservationStatus,
        @Param("expiryThreshold") expiryThreshold: LocalDateTime
    ): List<Reservation>
}
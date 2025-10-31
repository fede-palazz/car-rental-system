package com.rentalcarsystem.reservationservice.repositories

import com.rentalcarsystem.reservationservice.enums.ReservationStatus
import com.rentalcarsystem.reservationservice.models.Reservation
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
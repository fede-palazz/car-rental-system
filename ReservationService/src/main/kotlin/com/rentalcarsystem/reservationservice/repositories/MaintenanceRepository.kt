package com.rentalcarsystem.reservationservice.repositories

import com.rentalcarsystem.reservationservice.models.Maintenance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface MaintenanceRepository : JpaRepository<Maintenance, Long>, JpaSpecificationExecutor<Maintenance> {
}
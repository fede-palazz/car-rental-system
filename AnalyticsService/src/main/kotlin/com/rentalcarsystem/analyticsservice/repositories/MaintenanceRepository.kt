package com.rentalcarsystem.analyticsservice.repositories

import com.rentalcarsystem.analyticsservice.models.Maintenance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface MaintenanceRepository : JpaRepository<Maintenance, Long>, JpaSpecificationExecutor<Maintenance> {
}
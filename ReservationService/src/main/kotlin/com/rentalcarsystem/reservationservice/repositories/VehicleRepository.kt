package com.rentalcarsystem.reservationservice.repositories

import com.rentalcarsystem.reservationservice.models.Vehicle
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

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
}
package com.rentalcarsystem.analyticsservice.repositories

import com.rentalcarsystem.analyticsservice.models.CarModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface CarModelRepository : JpaRepository<CarModel, Long>, JpaSpecificationExecutor<CarModel> {
    fun findByBrandAndModelAndYear(
        brand: String,
        model: String,
        year: String
    ): CarModel?
}
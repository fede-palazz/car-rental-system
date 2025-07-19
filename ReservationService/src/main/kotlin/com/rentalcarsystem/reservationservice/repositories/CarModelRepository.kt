package com.rentalcarsystem.reservationservice.repositories

import com.rentalcarsystem.reservationservice.models.CarModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface CarModelRepository : JpaRepository<CarModel, Long>, JpaSpecificationExecutor<CarModel> {
    // When fetching the paginated car models, also fetch the features info
    // to avoid the N+1 problem
    @EntityGraph(attributePaths = ["features"])
    override fun findAll(spec: Specification<CarModel>?, pageable: Pageable): Page<CarModel>
    fun findByModel(model: String): MutableList<CarModel>
}
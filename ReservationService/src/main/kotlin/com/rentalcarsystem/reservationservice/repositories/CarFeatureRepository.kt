package com.rentalcarsystem.reservationservice.repositories

import com.rentalcarsystem.reservationservice.models.CarFeature
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CarFeatureRepository : JpaRepository<CarFeature, Long> {
}
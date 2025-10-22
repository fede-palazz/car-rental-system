package com.rentalcarsystem.trackingservice.repositories

import com.rentalcarsystem.trackingservice.models.TrackingPoint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TrackingPointRepository: JpaRepository<TrackingPoint, Long> {

}
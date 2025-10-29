package com.rentalcarsystem.trackingservice.services

import com.rentalcarsystem.trackingservice.dtos.request.SessionReqDTO
import com.rentalcarsystem.trackingservice.dtos.response.SessionResDTO
import com.rentalcarsystem.trackingservice.models.VehicleDailyDistance
import jakarta.validation.Valid
import java.time.LocalDate


interface TrackingService {

    fun getOngoingSessions(sortBy: String, sortOrder: String): List<SessionResDTO>

    fun getTrackingSession(sessionId: Long): SessionResDTO

    fun getOngoingTrackingSessionByUsername(customerUsername: String): SessionResDTO?

    fun getOngoingTrackingSessionByReservation(reservationId: Long): SessionResDTO?

    fun getOngoingTrackingSessionByVehicle(vehicleId: Long): SessionResDTO?

    fun createTrackingSession(@Valid sessionReq: SessionReqDTO): SessionResDTO

    fun endTrackingSession(@Valid sessionReq: SessionReqDTO): SessionResDTO

    fun getDailyVehicleDistances(date: LocalDate): List<VehicleDailyDistance>

}
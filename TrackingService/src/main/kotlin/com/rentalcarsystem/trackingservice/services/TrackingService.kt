package com.rentalcarsystem.trackingservice.services

import com.rentalcarsystem.trackingservice.dtos.request.SessionReqDTO
import com.rentalcarsystem.trackingservice.dtos.response.PagedResDTO
import com.rentalcarsystem.trackingservice.dtos.response.SessionResDTO
import jakarta.validation.Valid


interface TrackingService {

    fun getOngoingSessions(page: Int, size: Int, sortBy: String, sortOrder: String): PagedResDTO<SessionResDTO>

    fun getTrackingSession(sessionId: Long): SessionResDTO

    fun getOngoingTrackingSessionByUsername(customerUsername: String): SessionResDTO?

    fun getOngoingTrackingSessionByReservation(reservationId: Long): SessionResDTO?

    fun getOngoingTrackingSessionByVehicle(vehicleId: Long): SessionResDTO?

    fun createTrackingSession(@Valid sessionReq: SessionReqDTO): SessionResDTO

    fun endTrackingSession(sessionId: Long): SessionResDTO
}
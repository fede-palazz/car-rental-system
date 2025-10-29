package com.rentalcarsystem.trackingservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rentalcarsystem.trackingservice.dtos.request.SessionReqDTO
import com.rentalcarsystem.trackingservice.dtos.request.toEntity
import com.rentalcarsystem.trackingservice.dtos.response.PagedResDTO
import com.rentalcarsystem.trackingservice.dtos.response.SessionResDTO
import com.rentalcarsystem.trackingservice.dtos.response.toResDTO
import com.rentalcarsystem.trackingservice.exceptions.FailureException
import com.rentalcarsystem.trackingservice.exceptions.ResponseEnum
import com.rentalcarsystem.trackingservice.models.TrackingPoint
import com.rentalcarsystem.trackingservice.models.TrackingSession
import com.rentalcarsystem.trackingservice.repositories.TrackingPointRepository
import com.rentalcarsystem.trackingservice.repositories.TrackingSessionRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import java.time.Instant
import java.time.LocalDateTime


@Service
@Validated
class TrackingServiceImpl(
    private val trackingSessionRepository: TrackingSessionRepository,
    private val trackingPointRepository: TrackingPointRepository,
    private val objectMapper: ObjectMapper,
    ) : TrackingService {

    val turinCenterLat = 45.058360
    val turinCenterLng = 7.665896

    private val logger = LoggerFactory.getLogger(TrackingServiceImpl::class.java)

    override fun getOngoingSessions(
        sortBy: String,
        sortOrder: String
    ): List<SessionResDTO> {
        val sortDirection = if (sortOrder == "ASC") Sort.Direction.ASC else Sort.Direction.DESC
        val sort = Sort.by(sortDirection, sortBy)

        return trackingSessionRepository.findOngoingSessions(sort).map { session ->
            val lastPoint = trackingPointRepository
                .findTopByTrackingSessionIdOrderByTimestampDesc(session.getId()!!)
            session.toResDTO(lastPoint?.toResDTO())
        }
    }


    override fun getTrackingSession(sessionId: Long): SessionResDTO {
        return getSessionById(sessionId).toResDTO()
    }

    override fun getOngoingTrackingSessionByUsername(customerUsername: String): SessionResDTO? {
        return trackingSessionRepository
            .findOngoingSessionByUsername(customerUsername)
            ?.toResDTO()
    }

    override fun getOngoingTrackingSessionByReservation(reservationId: Long): SessionResDTO? {
        return trackingSessionRepository
            .findOngoingSessionByReservation(reservationId)
            ?.toResDTO()
    }

    override fun getOngoingTrackingSessionByVehicle(vehicleId: Long): SessionResDTO? {
        return trackingSessionRepository
            .findOngoingSessionByVehicle(vehicleId)
            ?.toResDTO()
    }

    override fun createTrackingSession(sessionReq: SessionReqDTO): SessionResDTO {
        // Check whether there are conflicting ongoing sessions
        if (trackingSessionRepository.hasConflictingSession(
                sessionReq.customerUsername,
                sessionReq.reservationId,
                sessionReq.vehicleId)
            )
            throw FailureException(ResponseEnum.SESSION_ALREADY_EXIST,
                "A tracking session with the same customer, vehicle or reservation already exists")

        val sessionToSave = sessionReq.toEntity()
        val startingPoint = TrackingPoint(
            lat = turinCenterLat,
            lng = turinCenterLng,
            timestamp = Instant.now(),
            bearing = 0.0,
            distanceIncremental = 0.0
        )
        sessionToSave.addTrackingPoint(startingPoint)
        return trackingSessionRepository.save(sessionToSave).toResDTO(startingPoint.toResDTO())
    }

    override fun endTrackingSession(sessionId: Long): SessionResDTO {
        val session = getSessionById(sessionId)

        session.endDate = LocalDateTime.now()
        trackingSessionRepository.saveAndFlush(session)

        return session.toResDTO()
    }

    private fun getSessionById(id: Long): TrackingSession {
        return trackingSessionRepository.findById(id)
            .orElseThrow {
                FailureException(ResponseEnum.SESSION_NOT_FOUND,
                    "Tracking session with ID $id not found")
            }
    }
}
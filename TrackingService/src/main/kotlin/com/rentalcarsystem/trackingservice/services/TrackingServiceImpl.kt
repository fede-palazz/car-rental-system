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
import com.rentalcarsystem.trackingservice.models.TrackingSession
import com.rentalcarsystem.trackingservice.repositories.TrackingSessionRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import java.time.LocalDateTime


@Service
@Validated
class TrackingServiceImpl(
    private val trackingSessionRepository: TrackingSessionRepository,
) : TrackingService {

    private val logger = LoggerFactory.getLogger(TrackingServiceImpl::class.java)
    private val objectMapper = ObjectMapper().registerKotlinModule()


    override fun getOngoingSessions(
        page: Int,
        size: Int,
        sortBy: String,
        sortOrder: String
    ): PagedResDTO<SessionResDTO> {
        // Sorting
        val sortOrd: Sort.Direction = if (sortOrder == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val pageable: Pageable = PageRequest.of(page, size, sortOrd, sortBy)
        val pageResult = trackingSessionRepository.findAll(pageable)

        return PagedResDTO(
            currentPage = pageResult.number,
            totalPages = pageResult.totalPages,
            totalElements = pageResult.totalElements,
            elementsInPage = pageResult.numberOfElements,
            content = pageResult.content.map { it.toResDTO() }
        )
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
        return trackingSessionRepository.save(sessionToSave).toResDTO()
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
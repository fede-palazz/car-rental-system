package com.rentalcarsystem.reservationservice.services

import com.rentalcarsystem.reservationservice.dtos.request.PaymentReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.SessionReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.UserUpdateReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.reservation.ActualPickUpDateReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.reservation.FinalizeReservationReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.reservation.ReservationReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.reservation.toEntity
import com.rentalcarsystem.reservationservice.dtos.response.PagedResDTO
import com.rentalcarsystem.reservationservice.dtos.response.PaymentRecordResDTO
import com.rentalcarsystem.reservationservice.dtos.response.PaymentResDTO
import com.rentalcarsystem.reservationservice.dtos.response.UserResDTO
import com.rentalcarsystem.reservationservice.dtos.response.reservation.CustomerReservationResDTO
import com.rentalcarsystem.reservationservice.dtos.response.reservation.StaffReservationResDTO
import com.rentalcarsystem.reservationservice.dtos.response.reservation.toCustomerReservationResDTO
import com.rentalcarsystem.reservationservice.dtos.response.reservation.toStaffReservationResDTO
import com.rentalcarsystem.reservationservice.enums.CarCategory
import com.rentalcarsystem.reservationservice.enums.CarStatus
import com.rentalcarsystem.reservationservice.enums.EventType
import com.rentalcarsystem.reservationservice.enums.ReservationStatus
import com.rentalcarsystem.reservationservice.exceptions.FailureException
import com.rentalcarsystem.reservationservice.exceptions.ResponseEnum
import com.rentalcarsystem.reservationservice.filters.PaymentRecordFilter
import com.rentalcarsystem.reservationservice.filters.ReservationFilter
import com.rentalcarsystem.reservationservice.kafka.ReservationEventDTO
import com.rentalcarsystem.reservationservice.models.*
import com.rentalcarsystem.reservationservice.repositories.ReservationRepository
import com.rentalcarsystem.reservationservice.repositories.VehicleRepository
import jakarta.persistence.criteria.Join
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.validation.annotation.Validated
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Service
@Transactional
@Validated
// TODO: Understand if and how to use locking mechanism to ensure concurrency control
class ReservationServiceImpl(
    private val carModelService: CarModelService,
    private val vehicleService: VehicleService,
    private val notificationService: NotificationService,
    private val vehicleRepository: VehicleRepository,
    private val reservationRepository: ReservationRepository,
    private val userManagementRestClient: RestClient,
    private val paymentServiceRestClient: RestClient,
    private val keycloakTokenRestClient: RestClient,
    private val trackingServiceRestClient: RestClient,
    private val taskScheduler: TaskScheduler,
    private val kafkaTemplate: KafkaTemplate<String, ReservationEventDTO>,
    @Value("\${reservation.buffer-days}")
    private val reservationBufferDays: Long,
    @Value("\${spring.security.oauth2.client.registration.keycloak.client-id}")
    private val clientId: String,
    @Value("\${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private val clientSecret: String,
    @Value("\${reservation.expiration-offset-minutes}")
    private val expirationOffsetMinutes: Long
) : ReservationService {
    private val logger = LoggerFactory.getLogger(ReservationServiceImpl::class.java)
    fun getAccessToken(): String {
        val body = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "client_credentials")
            add("client_id", clientId)
            add("client_secret", clientSecret)
        }
        //println(body)
        val response: Map<*, *> = keycloakTokenRestClient
            .post()
            .uri("") // Base URL already includes token path
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(body)
            .retrieve()
            .body(Map::class.java)!!

        return response["access_token"] as String
    }

    override fun getReservations(
        page: Int,
        size: Int,
        singlePage: Boolean,
        sortBy: String,
        sortOrder: String,
        isCustomer: Boolean,
        @Valid filters: ReservationFilter
    ): PagedResDTO<Any> {
        var spec: Specification<Reservation> = Specification.where(null)
        // Customer username
        filters.customerUsername?.takeIf { it.isNotBlank() }?.let { name ->
            spec = spec.and { root, _, cb ->
                cb.like(cb.lower(root.get("customerUsername")), "${name.lowercase()}%")
            }
        }
        // License plate
        filters.licensePlate?.takeIf { it.isNotBlank() }?.let { licensePlate ->
            spec = spec.and { root, _, cb ->
                val vehicleJoin: Join<Reservation, Vehicle> = root.join("vehicle")
                cb.like(cb.lower(vehicleJoin.get("licensePlate")), "${licensePlate.lowercase()}%")
            }
        }
        // VIN
        filters.vin?.takeIf { it.isNotBlank() }?.let { vin ->
            spec = spec.and { root, _, cb ->
                val vehicleJoin: Join<Reservation, Vehicle> = root.join("vehicle")
                cb.like(cb.lower(vehicleJoin.get("vin")), "${vin.lowercase()}%")
            }
        }
        // Brand
        filters.brand?.takeIf { it.isNotBlank() }?.let { brand ->
            spec = spec.and { root, _, cb ->
                val vehicleJoin: Join<Reservation, Vehicle> = root.join("vehicle")
                val carModelJoin: Join<Vehicle, CarModel> = vehicleJoin.join("carModel")
                cb.like(cb.lower(carModelJoin.get("brand")), "${brand.lowercase()}%")
            }
        }
        // Model
        filters.model?.takeIf { it.isNotBlank() }?.let { model ->
            spec = spec.and { root, _, cb ->
                val vehicleJoin: Join<Reservation, Vehicle> = root.join("vehicle")
                val carModelJoin: Join<Vehicle, CarModel> = vehicleJoin.join("carModel")
                cb.like(cb.lower(carModelJoin.get("model")), "${model.lowercase()}%")
            }
        }
        // Year
        filters.year?.let { year ->
            spec = spec.and { root, _, cb ->
                val vehicleJoin: Join<Reservation, Vehicle> = root.join("vehicle")
                val carModelJoin: Join<Vehicle, CarModel> = vehicleJoin.join("carModel")
                cb.equal(carModelJoin.get<Int>("year"), year)
            }
        }
        // Creation date
        filters.minCreationDate?.let { minCreationDate ->
            spec = spec.and { root, _, cb ->
                cb.greaterThanOrEqualTo(root.get("creationDate"), minCreationDate)
            }
        }
        filters.maxCreationDate?.let { maxCreationDate ->
            spec = spec.and { root, _, cb ->
                cb.lessThanOrEqualTo(root.get("creationDate"), maxCreationDate)
            }
        }
        // Planned pick-up date
        filters.minPlannedPickUpDate?.let { minPlannedPickUpDate ->
            spec = spec.and { root, _, cb ->
                cb.greaterThanOrEqualTo(root.get("plannedPickUpDate"), minPlannedPickUpDate)
            }
        }
        filters.maxPlannedPickUpDate?.let { maxPlannedPickUpDate ->
            spec = spec.and { root, _, cb ->
                cb.lessThanOrEqualTo(root.get("plannedPickUpDate"), maxPlannedPickUpDate)
            }
        }
        // Actual pick-up date
        filters.minActualPickUpDate?.let { minActualPickUpDate ->
            spec = spec.and { root, _, cb ->
                cb.greaterThanOrEqualTo(root.get("actualPickUpDate"), minActualPickUpDate)
            }
        }
        filters.maxActualPickUpDate?.let { maxActualPickUpDate ->
            spec = spec.and { root, _, cb ->
                cb.lessThanOrEqualTo(root.get("actualPickUpDate"), maxActualPickUpDate)
            }
        }
        // Planned drop-off date
        filters.minPlannedDropOffDate?.let { minPlannedDropOffDate ->
            spec = spec.and { root, _, cb ->
                cb.greaterThanOrEqualTo(root.get("plannedDropOffDate"), minPlannedDropOffDate)
            }
        }
        filters.maxPlannedDropOffDate?.let { maxPlannedDropOffDate ->
            spec = spec.and { root, _, cb ->
                cb.lessThanOrEqualTo(root.get("plannedDropOffDate"), maxPlannedDropOffDate)
            }
        }
        // Actual drop-off date
        filters.minActualDropOffDate?.let { minActualDropOffDate ->
            spec = spec.and { root, _, cb ->
                cb.greaterThanOrEqualTo(root.get("actualDropOffDate"), minActualDropOffDate)
            }
        }
        filters.maxActualDropOffDate?.let { maxActualDropOffDate ->
            spec = spec.and { root, _, cb ->
                cb.lessThanOrEqualTo(root.get("actualDropOffDate"), maxActualDropOffDate)
            }
        }
        // Buffered drop-off date
        filters.minBufferedDropOffDate?.let { minBufferedDropOffDate ->
            spec = spec.and { root, _, cb ->
                cb.greaterThanOrEqualTo(root.get("bufferedDropOffDate"), minBufferedDropOffDate)
            }
        }
        filters.maxBufferedDropOffDate?.let { maxBufferedDropOffDate ->
            spec = spec.and { root, _, cb ->
                cb.lessThanOrEqualTo(root.get("bufferedDropOffDate"), maxBufferedDropOffDate)
            }
        }
        // Status
        filters.status?.let { status ->
            spec = spec.and { root, _, cb ->
                cb.equal(root.get<ReservationStatus>("status"), status)
            }
        }
        // Total amount
        filters.minTotalAmount?.let { minPrice ->
            spec = spec.and { root, _, cb ->
                cb.greaterThanOrEqualTo(root.get("totalAmount"), minPrice)
            }
        }
        filters.maxTotalAmount?.let { maxPrice ->
            spec = spec.and { root, _, cb ->
                cb.lessThanOrEqualTo(root.get("totalAmount"), maxPrice)
            }
        }
        // Was delivery late
        filters.wasDeliveryLate?.let { wasDeliveryLate ->
            spec = spec.and { root, _, cb ->
                cb.equal(root.get<Boolean>("wasDeliveryLate"), wasDeliveryLate)
            }
        }
        // Was charged fee
        filters.wasChargedFee?.let { wasChargedFee ->
            spec = spec.and { root, _, cb ->
                cb.equal(root.get<Boolean>("wasChargedFee"), wasChargedFee)
            }
        }
        // Was involved in accident
        filters.wasInvolvedInAccident?.let { wasInvolvedInAccident ->
            spec = spec.and { root, _, cb ->
                cb.equal(root.get<Boolean>("wasInvolvedInAccident"), wasInvolvedInAccident)
            }
        }
        // Damage level
        filters.minDamageLevel?.let { minDamageLevel ->
            spec = spec.and { root, _, cb ->
                cb.greaterThanOrEqualTo(root.get("damageLevel"), minDamageLevel)
            }
        }
        filters.maxDamageLevel?.let { maxDamageLevel ->
            spec = spec.and { root, _, cb ->
                cb.lessThanOrEqualTo(root.get("damageLevel"), maxDamageLevel)
            }
        }
        // Dirtiness level
        filters.minDirtinessLevel?.let { minDirtinessLevel ->
            spec = spec.and { root, _, cb ->
                cb.greaterThanOrEqualTo(root.get("dirtinessLevel"), minDirtinessLevel)
            }
        }
        filters.maxDirtinessLevel?.let { maxDirtinessLevel ->
            spec = spec.and { root, _, cb ->
                cb.lessThanOrEqualTo(root.get("dirtinessLevel"), maxDirtinessLevel)
            }
        }
        // Pick-up staff username
        filters.pickUpStaffUsername?.takeIf { it.isNotBlank() }?.let { pickUpStaffUsername ->
            spec = spec.and { root, _, cb ->
                cb.like(cb.lower(root.get("pickUpStaffUsername")), "${pickUpStaffUsername.lowercase()}%")
            }
        }
        // Drop-off staff username
        filters.dropOffStaffUsername?.takeIf { it.isNotBlank() }?.let { dropOffStaffUsername ->
            spec = spec.and { root, _, cb ->
                cb.like(cb.lower(root.get("dropOffStaffUsername")), "${dropOffStaffUsername.lowercase()}%")
            }
        }
        // Sorting
        val sortOrd: Sort.Direction = if (sortOrder == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val sort: Sort = when (sortBy) {
            // Sorting on Car model fields
            in listOf("brand", "model", "year") -> {
                Sort.by(sortOrd, "vehicle.carModel.$sortBy")
            }
            // Sorting on Vehicle fields
            in listOf("licensePlate", "vin") -> {
                Sort.by(sortOrd, "vehicle.$sortBy")
            }
            // Sorting on Reservation fields
            else -> {
                Sort.by(sortOrd, sortBy)
            }
        }
        val actualSize = if (singlePage) reservationRepository.count().toInt() else size
        val pageable: Pageable = PageRequest.of(page, actualSize, sort)
        val pageResult = reservationRepository.findAll(spec, pageable)

        return PagedResDTO(
            currentPage = pageResult.number,
            totalPages = pageResult.totalPages,
            totalElements = pageResult.totalElements,
            elementsInPage = pageResult.numberOfElements,
            content = if (isCustomer) pageResult.content.map { it.toCustomerReservationResDTO() } else pageResult.content.map { it.toStaffReservationResDTO() }
        )
    }

    override fun getOverlappingReservations(
        vehicleId: Long,
        desiredStart: LocalDateTime,
        desiredEnd: LocalDateTime,
        page: Int,
        size: Int,
        singlePage: Boolean,
        sortBy: String,
        sortOrder: String,
        reservationToExcludeId: Long
    ): PagedResDTO<StaffReservationResDTO> {
        val vehicle = vehicleService.getVehicleById(vehicleId)
        var spec: Specification<Reservation> = Specification.where(null)
        // Vehicle
        spec = spec.and { root, _, cb ->
            cb.equal(root.get<Vehicle>("vehicle"), vehicle)
        }
        // Exclude the reservation reservationToExcludeId, or exclude none if reservationToExcludeId is not specified and defaults to 0
        spec = spec.and { root, _, cb ->
            cb.notEqual(root.get<Long>("id"), reservationToExcludeId)
        }
        // Only considers reservations that overlap the desired date range.
        spec = spec.and { root, _, cb ->
            cb.and(
                cb.lessThan(
                    root.get("plannedPickUpDate"),
                    desiredEnd
                ),
                cb.greaterThan(
                    root.get("plannedDropOffDate"),
                    desiredStart
                )
            )
        }
        // Sorting
        val sortOrd: Sort.Direction = if (sortOrder == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val sort: Sort = when (sortBy) {
            // Sorting on Car model fields
            in listOf("brand", "model", "year") -> {
                Sort.by(sortOrd, "vehicle.carModel.$sortBy")
            }
            // Sorting on Vehicle fields
            in listOf("licensePlate", "vin") -> {
                Sort.by(sortOrd, "vehicle.$sortBy")
            }
            // Sorting on Reservation fields
            else -> {
                Sort.by(sortOrd, sortBy)
            }
        }
        val actualSize = if (singlePage) reservationRepository.count().toInt() else size
        val pageable: Pageable = PageRequest.of(page, actualSize, sort)
        val pageResult = reservationRepository.findAll(spec, pageable)

        return PagedResDTO(
            currentPage = pageResult.number,
            totalPages = pageResult.totalPages,
            totalElements = pageResult.totalElements,
            elementsInPage = pageResult.numberOfElements,
            content = pageResult.content.map { it.toStaffReservationResDTO() }
        )
    }

    override fun createReservation(
        customerUsername: String,
        @Valid reservation: ReservationReqDTO
    ): CustomerReservationResDTO {
        if (reservationRepository.existsByCustomerUsernameAndStatus(customerUsername, ReservationStatus.PENDING)) {
            throw FailureException(
                ResponseEnum.RESERVATION_PENDING,
                "You must pay for your pending reservation before creating a new one"
            )
        }
        val vehicle = getVehicleByDesiredReservation(customerUsername, reservation)
        val days = ChronoUnit.DAYS.between(
            reservation.plannedPickUpDate.toLocalDate(),
            reservation.plannedDropOffDate.toLocalDate()
        ) + 1
        val reservationToSave =
            reservation.toEntity(vehicle.carModel.rentalPrice * days, customerUsername, reservationBufferDays)
        vehicle.addReservation(reservationToSave)
        val savedReservation = reservationRepository.save(reservationToSave)

        try {
            kafkaTemplate.send(
                "paypal.public.reservation-events",
                ReservationEventDTO(EventType.CREATED, savedReservation.toStaffReservationResDTO())
            )
        } catch (ex: Exception) {
            logger.error("Failed to send reservation creation event", ex)
        }

        return savedReservation.toCustomerReservationResDTO()
    }

    override fun setReservationActualPickUpDate(
        pickUpStaffUsername: String,
        reservationId: Long,
        @Valid actualPickUpDate: ActualPickUpDateReqDTO
    ): StaffReservationResDTO {
        val reservation = getReservationById(reservationId)
        // Check if reservation is still active (car paid and not returned yet)
        if (reservation.status != ReservationStatus.CONFIRMED) {
            throw FailureException(
                ResponseEnum.RESERVATION_WRONG_STATUS,
                "The reservation $reservationId was cancelled or not paid yet or has already been finalized"
            )
        }
        if (actualPickUpDate.actualPickUpDate.isBefore(reservation.plannedPickUpDate)) {
            throw IllegalArgumentException("The actual pick-up date cannot be before the planned pick-up date: ${reservation.plannedPickUpDate}")
        }
        if (actualPickUpDate.actualPickUpDate.isAfter(reservation.plannedDropOffDate)) {
            throw IllegalArgumentException("The actual pick-up date cannot be after the planned drop-off date: ${reservation.plannedDropOffDate}")
        }
        reservation.actualPickUpDate = actualPickUpDate.actualPickUpDate
        reservation.status = ReservationStatus.PICKED_UP
        reservation.pickUpStaffUsername = pickUpStaffUsername
        val savedReservation = reservation.toStaffReservationResDTO()

        try {
            val token = getAccessToken()
            val response = trackingServiceRestClient.post().uri("/sessions/start").body(
                SessionReqDTO(
                    vehicleId = reservation.vehicle?.getId()!!,
                    reservationId = reservationId,
                    customerUsername = reservation.customerUsername
                )
            ).header(HttpHeaders.AUTHORIZATION, "Bearer $token").accept(
                APPLICATION_JSON
            ).retrieve().body<Any>()
        } catch (e: Exception) {
            logger.error("Failed to send session request ${e.message}")
            throw FailureException(
                ResponseEnum.TRACKING_ERROR,
                e.message
            )
        }

        try {
            kafkaTemplate.send(
                "paypal.public.reservation-events",
                ReservationEventDTO(EventType.PICKED_UP, savedReservation)
            )
        } catch (ex: Exception) {
            logger.error("Failed to send reservation pick-up event", ex)
        }

        return savedReservation
    }

    override fun finalizeReservation(
        dropOffStaffUsername: String,
        reservationId: Long,
        @Valid finalizeReq: FinalizeReservationReqDTO
    ): StaffReservationResDTO {
        val reservation = getReservationById(reservationId)
        if (reservation.status != ReservationStatus.PICKED_UP) {
            throw FailureException(
                ResponseEnum.RESERVATION_WRONG_STATUS,
                "The vehicle of reservation $reservationId has not been picked up yet"
            )
        }
        if (finalizeReq.actualDropOffDate.isBefore(reservation.actualPickUpDate)) {
            throw IllegalArgumentException("The actual drop-off date cannot be before the actual pick-up date: ${reservation.actualPickUpDate}")
        }
        val overlappingReservationsAmount = getOverlappingReservations(
            vehicleId = reservation.vehicle?.getId()!!,
            desiredStart = reservation.actualPickUpDate!!,
            desiredEnd = finalizeReq.bufferedDropOffDate,
            page = 0,
            size = 1,
            singlePage = false,
            sortBy = "creationDate",
            sortOrder = "asc",
            reservationToExcludeId = reservationId
        ).totalElements
        if (overlappingReservationsAmount > 0) {
            throw FailureException(
                ResponseEnum.RESERVATION_CONFLICT,
                "The vehicle of reservation $reservationId has $overlappingReservationsAmount overlapping reservations"
            )
        }
        reservation.actualDropOffDate = finalizeReq.actualDropOffDate
        reservation.bufferedDropOffDate = finalizeReq.bufferedDropOffDate
        reservation.status = ReservationStatus.DELIVERED
        reservation.wasDeliveryLate = finalizeReq.wasDeliveryLate
        reservation.wasChargedFee = finalizeReq.wasChargedFee
        reservation.wasInvolvedInAccident = finalizeReq.wasInvolvedInAccident
        reservation.damageLevel = finalizeReq.damageLevel
        reservation.dirtinessLevel = finalizeReq.dirtinessLevel
        reservation.dropOffStaffUsername = dropOffStaffUsername
        val token = getAccessToken()
        val customer = userManagementRestClient.get().uri("/username/{username}", reservation.customerUsername)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token").accept(
                APPLICATION_JSON
            ).retrieve().body<UserResDTO>()
        var newScore = customer?.eligibilityScore!!
        if (reservation.wasDeliveryLate == true) {
            newScore -= WAS_DELIVERY_LATE_PENALTY
        }
        if (reservation.wasChargedFee == true) {
            newScore -= WAS_CHARGED_FEE_PENALTY
        }
        if (reservation.wasInvolvedInAccident == true) {
            newScore -= WAS_INVOLVED_IN_ACCIDENT_PENALTY
        }
        reservation.damageLevel?.let {
            if (it > 0) {
                newScore -= WAS_VEHICLE_DAMAGED_PENALTY * it
            }
        }
        reservation.dirtinessLevel?.let {
            if (it > 0) {
                newScore -= WAS_VEHICLE_DIRTY_PENALTY * it
            }
        }
        if (reservation.wasDeliveryLate != true && reservation.wasChargedFee != true
            && reservation.wasInvolvedInAccident != true && reservation.damageLevel == 0 && reservation.dirtinessLevel == 0
        ) {
            newScore += NO_PROBLEM_BONUS
        }
        if (newScore < 0) {
            newScore = 0.0
        } else if (newScore > 100) {
            newScore = 100.0
        }
        val updatedCustomer = UserUpdateReqDTO(
            firstName = customer.firstName,
            lastName = customer.lastName,
            phone = customer.phone,
            address = customer.address,
            role = customer.role,
            eligibilityScore = newScore
        )
        val updatedCustomerRes =
            userManagementRestClient.put().uri("/{userId}", customer.id).body(updatedCustomer)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token").accept(
                    APPLICATION_JSON
                ).retrieve().body<UserResDTO>()

        if (updatedCustomerRes != null && updatedCustomerRes.eligibilityScore != newScore) {
            throw RuntimeException("Failed to update customer's score")
        }
        if (reservation.bufferedDropOffDate.isAfter(LocalDateTime.now())) {
            reservation.vehicle?.let {
                reservation.vehicle?.pendingCleaning = true
                scheduleVehicleAvailabilityUpdate(it, reservation.bufferedDropOffDate)
            }
        } else {
            if (reservation.vehicle?.status == CarStatus.RENTED) {
                reservation.vehicle?.status = CarStatus.AVAILABLE
            }
            if (reservation.vehicle?.pendingCleaning == true) {
                reservation.vehicle?.pendingCleaning = false
            }
        }
        val savedReservation = reservation.toStaffReservationResDTO()

        try {
            val token = getAccessToken()
            trackingServiceRestClient.post().uri("/sessions/end").body(
                SessionReqDTO(
                    vehicleId = reservation.vehicle?.getId()!!,
                    reservationId = reservationId,
                    customerUsername = reservation.customerUsername
                )
            ).header(HttpHeaders.AUTHORIZATION, "Bearer $token").accept(
                APPLICATION_JSON
            ).retrieve().body<Any>()
        } catch (e: Exception) {
            logger.error("Failed to send session request ${e.message}")
            throw FailureException(
                ResponseEnum.TRACKING_ERROR,
                e.message
            )
        }

        try {
            kafkaTemplate.send(
                "paypal.public.reservation-events",
                ReservationEventDTO(EventType.FINALIZED, savedReservation)
            )
        } catch (ex: Exception) {
            logger.error("Failed to send reservation finalized event", ex)
        }

        return savedReservation
    }

    override fun updateReservationVehicle(
        updatedVehicleStaffUsername: String,
        reservationId: Long,
        vehicleId: Long
    ): StaffReservationResDTO {
        val reservation = getReservationById(reservationId)
        if (reservation.status == ReservationStatus.CANCELLED
            || reservation.status == ReservationStatus.PICKED_UP
            || reservation.status == ReservationStatus.DELIVERED
        ) {
            throw FailureException(
                ResponseEnum.RESERVATION_WRONG_STATUS,
                "Cannot update reservation $reservationId as it has already been cancelled, picked up or delivered"
            )
        }
        val vehicle = vehicleRepository.findAvailableVehicleByIdAndDateRange(
            vehicleId = vehicleService.getVehicleById(vehicleId).getId()!!,
            reservationToExcludeId = reservation.getId()!!,
            desiredEndWithBuffer = reservation.plannedDropOffDate.plusDays(reservationBufferDays),
            desiredStart = reservation.plannedPickUpDate,
            desiredEnd = reservation.plannedDropOffDate
        ) ?: throw FailureException(
            ResponseEnum.VEHICLE_NOT_AVAILABLE,
            "The requested vehicle is not available for the selected dates"
        )
        reservation.updatedVehicleStaffUsername = updatedVehicleStaffUsername
        vehicle.addReservation(reservation)
        val savedReservation = reservation.toStaffReservationResDTO()

        try {
            kafkaTemplate.send(
                "paypal.public.reservation-events",
                ReservationEventDTO(EventType.UPDATED, savedReservation)
            )
        } catch (ex: Exception) {
            logger.error("Failed to send reservation updated vehicle event", ex)
        }

        return savedReservation
    }

    override fun deleteReservation(reservationId: Long) {
        val reservation = getReservationById(reservationId)
        val reservationToDelete = reservation.toStaffReservationResDTO()
        if (reservation.plannedPickUpDate.isBefore(LocalDateTime.now())) {
            throw FailureException(
                ResponseEnum.RESERVATION_FORBIDDEN,
                "You are not allowed to delete reservation $reservationId as it has already started"
            )
        }
        reservation.vehicle?.removeReservation(reservation)

        try {
            kafkaTemplate.send(
                "paypal.public.reservation-events",
                ReservationEventDTO(EventType.DELETED, reservationToDelete)
            )
        } catch (ex: Exception) {
            logger.error("Failed to send reservation deleted event", ex)
        }
    }

    override fun createPaymentRequest(reservationId: Long, customerUsername: String): PaymentResDTO {
        // TODO: Replace paymentReqDTO.customerId with logged in user's id and completely remove paymentReqDTO from the request body in the controller
        val reservation = getReservationById(reservationId)

        if (reservation.status != ReservationStatus.PENDING) {
            throw FailureException(
                ResponseEnum.RESERVATION_FORBIDDEN,
                "You are not allowed to create a payment request for reservation $reservationId as it has already started"
            )
        }
        val carModel = reservation.vehicle!!.carModel

        val paymentReqDTO = PaymentReqDTO(reservationId)
        paymentReqDTO.customerUsername = customerUsername
        paymentReqDTO.amount = reservation.totalAmount
        paymentReqDTO.description =
            "Customer $customerUsername pays reservation for ${carModel.brand} ${carModel.model} of total amount: ${paymentReqDTO.amount}"
        val paymentRes: PaymentResDTO?
        try {
            val token = getAccessToken()
            paymentRes = paymentServiceRestClient.post().uri("/create").body(paymentReqDTO)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token").accept(
                    APPLICATION_JSON
                ).retrieve().body<PaymentResDTO>()
        } catch (e: Exception) {
            logger.error("Failed to create a payment request ${e.message}")
            throw FailureException(
                ResponseEnum.PAYMENT_ERROR,
                e.message
            ) // TODO: Handle different payment errors with respective ResponseEnum instead of this single generic one?
        }

        if (paymentRes == null) {
            throw RuntimeException("Failed to create a payment request") // TODO: Better handle this exception
        }
        return paymentRes
    }

    override fun getPaymentRecordByReservationId(reservationId: Long): PaymentRecordResDTO {
        try {
            val token = getAccessToken()
            return paymentServiceRestClient.get().uri("/order/reservation/{reservationId}", reservationId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token").accept(
                    APPLICATION_JSON
                ).retrieve().body<PaymentRecordResDTO>()!!
        } catch (e: Exception) {
            logger.error("Failed to create a payment request ${e.message}")
            throw FailureException(
                ResponseEnum.PAYMENT_ERROR,
                e.message
            ) // TODO: Handle different payment errors with respective ResponseEnum instead of this single generic one?
        }
    }

    override fun getPaymentRecordByToken(token: String): PaymentRecordResDTO {
        try {
            val jwtToken = getAccessToken()
            return paymentServiceRestClient.get().uri("/order/token/{token}", token)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken").accept(
                    APPLICATION_JSON
                ).retrieve().body<PaymentRecordResDTO>()!!
        } catch (e: Exception) {
            logger.error("Failed to create a payment request ${e.message}")
            throw FailureException(
                ResponseEnum.PAYMENT_ERROR,
                e.message
            ) // TODO: Handle different payment errors with respective ResponseEnum instead of this single generic one?
        }
    }

    override fun getReservationById(id: Long): Reservation {
        return reservationRepository.findById(id).orElseThrow {
            FailureException(ResponseEnum.RESERVATION_NOT_FOUND, "Reservation with id $id was not found")
        }
    }

    override fun confirmReservation(id: Long) {
        val reservation = getReservationById(id)
        reservation.status = ReservationStatus.CONFIRMED

        val customer = getCustomerByUsername(reservation.customerUsername)!!

        val payload = ReservationEventDTO(
            EventType.CONFIRMED,
            reservation.toStaffReservationResDTO()
        )

        try {
            kafkaTemplate.send(
                "paypal.public.reservation-events",
                payload
            )
            notificationService.sendReservationConfirmedEmail(
                customer.email,
                "${customer.firstName} ${customer.lastName}",
                payload)
        } catch (ex: Exception) {
            logger.error("Failed to send reservation confirmed event", ex)
        }
    }

    override fun getPaymentRecords(
        page: Int,
        size: Int,
        sortBy: String,
        sortOrder: String,
        filters: PaymentRecordFilter
    ): PagedResDTO<PaymentRecordResDTO> {
        var uri = "/records?page=$page&size=$size&sort=$sortBy&order=$sortOrder"
        filters.reservationId?.let { reservationId ->
            uri = "$uri&reservationId=$reservationId"
        }
        filters.customerUsername?.let { customerUsername ->
            uri = "$uri&customerUsername=$customerUsername"
        }
        filters.minAmount?.let { minAmount ->
            uri = "$uri&minAmount=$minAmount"
        }
        filters.maxAmount?.let { maxAmount ->
            uri = "$uri&maxAmount=$maxAmount"
        }
        filters.token?.let { token ->
            uri = "$uri&token=$token"
        }
        filters.status?.let { status ->
            uri = "$uri&status=$status"
        }
        try {
            val token = getAccessToken()
            return paymentServiceRestClient.get().uri(uri).header(HttpHeaders.AUTHORIZATION, "Bearer $token").accept(
                APPLICATION_JSON
            ).retrieve().body<PagedResDTO<PaymentRecordResDTO>>()!!
        } catch (e: Exception) {
            logger.error("Failed to create a payment request ${e.message}")
            throw FailureException(
                ResponseEnum.PAYMENT_ERROR,
                e.message
            ) // TODO: Handle different payment errors with respective ResponseEnum instead of this single generic one?
        }
    }

    /*
    *******************************************************
     */

    private fun getVehicleByDesiredReservation(
        customerUsername: String,
        reservation: ReservationReqDTO,
        reservationToUpdateId: Long = 0L // Provided when updating a reservation, so that the updated reservation is not considered as overlapping with itself
    ): Vehicle {
        if (!reservation.plannedDropOffDate.isAfter(reservation.plannedPickUpDate)) {
            throw IllegalArgumentException("The drop-off date must be after the pick-up date")
        }
        val carModel = carModelService.getActualCarModelById(reservation.carModelId)
        val token = getAccessToken()
        val userScore = userManagementRestClient.get().uri("/username/{username}", customerUsername)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token").accept(
                APPLICATION_JSON
            ).retrieve().body<UserResDTO>()?.eligibilityScore

        if (userScore!! < CarCategory.getValue(carModel.category)!!) {
            throw FailureException(
                ResponseEnum.RESERVATION_INSUFFICIENT_SCORE,
                "Your score is too low to reserve a vehicle of category ${carModel.category}"
            )
        }
        val vehicle = vehicleRepository.findAvailableVehiclesByModelAndDateRange(
            carModel = carModel,
            desiredEndWithBuffer = reservation.plannedDropOffDate.plusDays(reservationBufferDays),
            desiredStart = reservation.plannedPickUpDate,
            desiredEnd = reservation.plannedDropOffDate,
            pageable = PageRequest.of(0, 1, Sort.Direction.ASC, "kmTravelled")
        ).content.firstOrNull()
        if (vehicle == null) {
            throw FailureException(
                ResponseEnum.CAR_MODEL_NOT_AVAILABLE,
                "The requested model is not available for the selected dates"
            )
        }
        return vehicle
    }

    @Transactional
    @Scheduled(fixedRate = 10 * 60 * 1000) // runs every 10 minutes
    fun expireOldReservations() {
        val expirationThreshold = LocalDateTime.now().minusMinutes(expirationOffsetMinutes)
        val expiredReservations = reservationRepository.findExpiredReservations(
            activeStatus = ReservationStatus.PENDING,
            expiryThreshold = expirationThreshold
        )
        for (reservation in expiredReservations) {
            reservation.status = ReservationStatus.EXPIRED
            logger.info("Set Reservation {} as expired", reservation.getId()!!)

            try {
                kafkaTemplate.send(
                    "paypal.public.reservation-events",
                    ReservationEventDTO(EventType.EXPIRED, reservation.toStaffReservationResDTO())
                )
            } catch (ex: Exception) {
                logger.error("Failed to send reservation expired event", ex)
            }
        }
    }

    fun scheduleVehicleAvailabilityUpdate(vehicle: Vehicle, bufferedDropOffDate: LocalDateTime) {
        val runAt = bufferedDropOffDate.atZone(ZoneId.of("Europe/Rome")).toInstant()
        taskScheduler.schedule({
            if (vehicle.status == CarStatus.RENTED) {
                vehicle.status = CarStatus.AVAILABLE
                logger.info("Set Vehicle {} as available", vehicle.getId()!!)
            }
            if (vehicle.pendingCleaning) {
                vehicle.pendingCleaning = false
                logger.info("Set Vehicle {} pending cleaning to false", vehicle.getId()!!)
            }
            vehicleRepository.save(vehicle)
        }, runAt)
    }

    private fun getCustomerByUsername(username: String): UserResDTO? {
        val token = getAccessToken()
        return userManagementRestClient.get().uri("/username/{username}", username)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token").accept(
                APPLICATION_JSON
            ).retrieve().body<UserResDTO>()
    }
}

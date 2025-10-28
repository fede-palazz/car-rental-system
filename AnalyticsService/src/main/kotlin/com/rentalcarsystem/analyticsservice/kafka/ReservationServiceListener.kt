package com.rentalcarsystem.analyticsservice.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.rentalcarsystem.analyticsservice.enums.EventType
import com.rentalcarsystem.analyticsservice.exceptions.FailureException
import com.rentalcarsystem.analyticsservice.exceptions.ResponseEnum
import com.rentalcarsystem.analyticsservice.models.CarModel
import com.rentalcarsystem.analyticsservice.models.Maintenance
import com.rentalcarsystem.analyticsservice.models.Reservation
import com.rentalcarsystem.analyticsservice.repositories.CarModelRepository
import com.rentalcarsystem.analyticsservice.repositories.MaintenanceRepository
import com.rentalcarsystem.analyticsservice.repositories.ReservationRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.slf4j.LoggerFactory

@Component
class ReservationServiceListener(
    private val carModelRepository: CarModelRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val reservationRepository: ReservationRepository
) {
    private val logger = LoggerFactory.getLogger(ReservationServiceListener::class.java)
    private val mapper = ObjectMapper().apply { registerModule(JavaTimeModule()) }

    @KafkaListener(
        topics = ["paypal.public.car-model-events"],
        containerFactory = "carModelKafkaListenerContainerFactory",
        groupId = "\${spring.kafka.consumer.group-id:paymentService}"
    )
    @Transactional
    fun processCarModelEvents(event: CarModelEventDTO) {
        try {
/*          println("STARTED RECEIVING MESSAGE")
            println("MESSAGE: Type: ${event.type} CarModel: ${event.carModel} CompositeId: ${event.compositeId}")
            println("FINISHED RECEIVING MESSAGE")*/
            when (event.type) {
                EventType.CREATED -> {
                    if (event.carModel == null) {
                        throw FailureException(ResponseEnum.CAR_MODEL_NOT_PROVIDED, "Car model not provided!")
                    }
                    val newCarModel = event.carModel.toEntity()
                    carModelRepository.save(newCarModel)
                    logger.info("Created car model from event: {}", mapper.writeValueAsString(newCarModel))
                }
                EventType.UPDATED -> {
                    if (event.carModel == null) {
                        throw FailureException(ResponseEnum.CAR_MODEL_NOT_PROVIDED, "Car model not provided!")
                    }
                    val modelToUpdate = getCarModelByCompositeId(event.compositeId)
                    modelToUpdate.brand = event.carModel.brand
                    modelToUpdate.model = event.carModel.model
                    modelToUpdate.year = event.carModel.year
                    modelToUpdate.segment = event.carModel.segment
                    modelToUpdate.category = event.carModel.category
                    modelToUpdate.engineType = event.carModel.engineType
                    modelToUpdate.transmissionType = event.carModel.transmissionType
                    modelToUpdate.drivetrain = event.carModel.drivetrain
                    modelToUpdate.rentalPrice = event.carModel.rentalPrice
                    logger.info("Updated car model from event: {}", mapper.writeValueAsString(modelToUpdate))
                }
                EventType.DELETED -> {
                    val modelToDelete = getCarModelByCompositeId(event.compositeId)
                    carModelRepository.delete(modelToDelete)
                    logger.info("Deleted car model from event: {}", mapper.writeValueAsString(modelToDelete))
                }
                else -> {
                    throw FailureException(
                        ResponseEnum.INVALID_EVENT_TYPE,
                        "Invalid event type: ${event.type} for topic car-model-events"
                    )
                }
            }
        } catch (e: Exception) {
            // Log the error
            logger.error("Error processing car model event: ${e.message}")
        }
    }

    @KafkaListener(
        topics = ["paypal.public.maintenance-events"],
        containerFactory = "maintenanceKafkaListenerContainerFactory",
        groupId = "\${spring.kafka.consumer.group-id:paymentService}",
    )
    @Transactional
    fun processMaintenanceEvents(event: MaintenanceEventDTO) {
        try {
/*          println("STARTED RECEIVING MESSAGE")
            println("MESSAGE: Type: ${event.type} Maintenance: ${event.maintenance}")
            println("FINISHED RECEIVING MESSAGE")*/
            when (event.type) {
                EventType.CREATED -> {
                    if (event.startFleetManagerUsername == null) {
                        throw FailureException(ResponseEnum.MAINTENANCE_NOT_PROVIDED, "startFleetManagerUsername not provided!")
                    }
                    val newMaintenance = event.maintenance.toEntity(event.startFleetManagerUsername)
                    maintenanceRepository.save(newMaintenance)
                    logger.info("Created maintenance from event: {}", mapper.writeValueAsString(newMaintenance))
                }
                EventType.FINALIZED -> {
                    if (event.endFleetManagerUsername == null) {
                        throw FailureException(ResponseEnum.MAINTENANCE_NOT_PROVIDED, "endFleetManagerUsername not provided!")
                    }
                    val maintenanceToUpdate = getMaintenanceById(event.maintenance.id)
                    maintenanceToUpdate.actualEndDate = event.maintenance.actualEndDate
                    maintenanceToUpdate.endFleetManagerUsername = event.endFleetManagerUsername
                    logger.info("Updated maintenance from event: {}", mapper.writeValueAsString(maintenanceToUpdate))
                }
                EventType.UPDATED -> {
                    val maintenanceToUpdate = getMaintenanceById(event.maintenance.id)
                    maintenanceToUpdate.type = event.maintenance.type
                    maintenanceToUpdate.startDate = event.maintenance.startDate
                    maintenanceToUpdate.plannedEndDate = event.maintenance.plannedEndDate
                    logger.info("Updated maintenance from event: {}", mapper.writeValueAsString(maintenanceToUpdate))
                }
                EventType.DELETED -> {
                    val maintenanceToDelete = getMaintenanceById(event.maintenance.id)
                    maintenanceRepository.delete(maintenanceToDelete)
                    logger.info("Deleted maintenance from event: {}", mapper.writeValueAsString(maintenanceToDelete))
                }
                else -> {
                    throw FailureException(
                        ResponseEnum.INVALID_EVENT_TYPE,
                        "Invalid event type: ${event.type} for topic maintenance-events"
                    )
                }
            }
        } catch (e: Exception) {
            // Log the error
            logger.error("Error processing car model event: ${e.message}")
        }
    }

    @KafkaListener(
        topics = ["paypal.public.reservation-events"],
        containerFactory = "reservationKafkaListenerContainerFactory",
        groupId = "\${spring.kafka.consumer.group-id:paymentService}",
    )
    @Transactional
    fun processReservationEvents(event: ReservationEventDTO) {
        try {
/*          println("STARTED RECEIVING MESSAGE")
            println("MESSAGE: Type: ${event.type} Reservation: ${event.reservation}")
            println("FINISHED RECEIVING MESSAGE")*/
            when (event.type) {
                EventType.CREATED -> {
                    val newReservation = event.reservation.toEntity()
                    reservationRepository.save(newReservation)
                    logger.info("Created reservation from event: {}", mapper.writeValueAsString(newReservation))
                }
                EventType.PICKED_UP -> {
                    val reservationToUpdate = getReservationById(event.reservation.commonInfo.id)
                    reservationToUpdate.actualPickUpDate = event.reservation.commonInfo.actualPickUpDate
                    reservationToUpdate.status = event.reservation.commonInfo.status
                    reservationToUpdate.pickUpStaffUsername = event.reservation.pickUpStaffUsername
                    logger.info("Picked up reservation from event: {}", mapper.writeValueAsString(reservationToUpdate))
                }
                EventType.FINALIZED -> {
                    val reservationToUpdate = getReservationById(event.reservation.commonInfo.id)
                    reservationToUpdate.actualDropOffDate = event.reservation.commonInfo.actualDropOffDate
                    reservationToUpdate.bufferedDropOffDate = event.reservation.bufferedDropOffDate
                    reservationToUpdate.status = event.reservation.commonInfo.status
                    reservationToUpdate.wasDeliveryLate = event.reservation.wasDeliveryLate
                    reservationToUpdate.wasChargedFee = event.reservation.wasChargedFee
                    reservationToUpdate.wasInvolvedInAccident = event.reservation.wasInvolvedInAccident
                    reservationToUpdate.damageLevel = event.reservation.damageLevel
                    reservationToUpdate.dirtinessLevel = event.reservation.dirtinessLevel
                    reservationToUpdate.dropOffStaffUsername = event.reservation.dropOffStaffUsername
                    logger.info("Finalized reservation from event: {}", mapper.writeValueAsString(reservationToUpdate))
                }
                EventType.UPDATED -> {
                    val reservationToUpdate = getReservationById(event.reservation.commonInfo.id)
                    reservationToUpdate.updatedVehicleStaffUsername = event.reservation.updatedVehicleStaffUsername
                    logger.info("Updated reservation from event: {}", mapper.writeValueAsString(reservationToUpdate))
                }
                EventType.DELETED -> {
                    val reservationToDelete = getReservationById(event.reservation.commonInfo.id)
                    reservationRepository.delete(reservationToDelete)
                    logger.info("Deleted reservation from event: {}", mapper.writeValueAsString(reservationToDelete))
                }
                EventType.CONFIRMED -> {
                    val reservationToUpdate = getReservationById(event.reservation.commonInfo.id)
                    reservationToUpdate.status = event.reservation.commonInfo.status
                    logger.info("Confirmed reservation from event: {}", mapper.writeValueAsString(reservationToUpdate))
                }
                EventType.EXPIRED -> {
                    val reservationToUpdate = getReservationById(event.reservation.commonInfo.id)
                    reservationToUpdate.status = event.reservation.commonInfo.status
                    logger.info("Expired reservation from event: {}", mapper.writeValueAsString(reservationToUpdate))
                }
            }
        } catch (e: Exception) {
            // Log the error
            logger.error("Error processing reservation event: ${e.message}")
        }
    }

    /*************************************************************************************************************/

    fun getCarModelByCompositeId(compositeId: String?): CarModel {
        val compositeId = compositeId?.split(",")
        if (compositeId == null || compositeId.size != 3) {
            throw FailureException(
                ResponseEnum.WRONG_CAR_MODEL_COMPOSITE_ID,
                "Invalid composite id for car model update event: $compositeId"
            )
        }
        // Check if car model exists
        val model = carModelRepository.findByBrandAndModelAndYear(
            compositeId[0], compositeId[1], compositeId[2]
        )
        if (model == null) {
            throw FailureException(
                ResponseEnum.CAR_MODEL_NOT_FOUND,
                "Car model ${compositeId[0]} ${compositeId[1]} ${compositeId[2]} not found!"
            )
        }
        return model
    }

    fun getMaintenanceById(maintenanceId: Long): Maintenance {
        return maintenanceRepository.findById(maintenanceId).orElseThrow {
            FailureException(ResponseEnum.MAINTENANCE_NOT_FOUND, "Maintenance record with id $maintenanceId not found")
        }
    }

    fun getReservationById(reservationId: Long): Reservation {
        return reservationRepository.findById(reservationId).orElseThrow {
            FailureException(ResponseEnum.RESERVATION_NOT_FOUND, "Reservation with id $reservationId was not found")
        }
    }
}
package com.rentalcarsystem.reservationservice.integration.services

import com.rentalcarsystem.reservationservice.enums.CarCategory
import com.rentalcarsystem.reservationservice.enums.CarSegment
import com.rentalcarsystem.reservationservice.enums.CarStatus
import com.rentalcarsystem.reservationservice.enums.Drivetrain
import com.rentalcarsystem.reservationservice.enums.EngineType
import com.rentalcarsystem.reservationservice.enums.ReservationStatus
import com.rentalcarsystem.reservationservice.enums.TransmissionType
import com.rentalcarsystem.reservationservice.filters.ReservationFilter
import com.rentalcarsystem.reservationservice.integration.BaseIntegrationTest
import com.rentalcarsystem.reservationservice.models.CarModel
import com.rentalcarsystem.reservationservice.models.Reservation
import com.rentalcarsystem.reservationservice.models.Vehicle
import com.rentalcarsystem.reservationservice.repositories.CarModelRepository
import com.rentalcarsystem.reservationservice.repositories.ReservationRepository
import com.rentalcarsystem.reservationservice.repositories.VehicleRepository
import com.rentalcarsystem.reservationservice.services.ReservationService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Transactional
class ReservationServiceIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var reservationService: ReservationService

    @Autowired
    private lateinit var reservationRepository: ReservationRepository

    @Autowired
    lateinit var carModelRepository: CarModelRepository

    @Autowired
    lateinit var vehicleRepository: VehicleRepository


    private lateinit var carModel: CarModel
    private lateinit var vehicle: Vehicle
    private lateinit var testReservation: Reservation

    @BeforeEach
    fun setup() {
        carModel = carModelRepository.save(
            CarModel(
                brand = "Toyota",
                model = "Yaris",
                year = "2022",
                segment = CarSegment.COMPACT,
                doorsNumber = 4,
                seatingCapacity = 5,
                luggageCapacity = 100.0,
                category = CarCategory.ECONOMY,
                engineType = EngineType.ELECTRIC,
                transmissionType = TransmissionType.AUTOMATIC,
                drivetrain = Drivetrain.FWD,
                motorDisplacement = 4,
                rentalPrice = 50.0
            )
        )

        vehicle = vehicleRepository.save(
            Vehicle(
                licensePlate = "XYZ1234",
                vin = "1HGBH41JXMN109186",
                carModel = carModel,
                status = CarStatus.AVAILABLE,
                kmTravelled = 15000.0,
                pendingCleaning = false,
                pendingRepair = false
            )
        )

        testReservation = reservationRepository.save(
            Reservation(
                customerId = 1L,
                vehicle = vehicle,
                status = ReservationStatus.CONFIRMED,
                plannedPickUpDate = LocalDateTime.now(ZoneOffset.UTC).plusDays(1),
                plannedDropOffDate = LocalDateTime.now(ZoneOffset.UTC).plusDays(3),
                creationDate = LocalDateTime.now(ZoneOffset.UTC)
            )
        )
    }

    @AfterEach
    fun cleanup() {
        reservationRepository.deleteAll()
        vehicleRepository.deleteAll()
        carModelRepository.deleteAll()
    }

    @Test
    fun `test getReservations returns correct filtered result`() {
        val filter = ReservationFilter(licensePlate = "XYZ")
        val result = reservationService.getReservations(
            customerId = 1L,
            page = 0,
            size = 10,
            sortBy = "creationDate",
            sortOrder = "asc",
            filters = filter
        )

        assertNotNull(result)
        assertTrue(result.content.isNotEmpty())
        assertEquals(1, result.totalElements)
    }

    @Test
    fun `test getReservations returns empty when filter does not match`() {
        val filter = ReservationFilter(licensePlate = "ABC")
        val result = reservationService.getReservations(
            customerId = 1L,
            page = 0,
            size = 10,
            sortBy = "creationDate",
            sortOrder = "asc",
            filters = filter
        )

        assertNotNull(result)
        assertTrue(result.content.isEmpty())
    }

}
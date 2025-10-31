package com.rentalcarsystem.reservationservice.integration.controllers

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rentalcarsystem.reservationservice.integration.BaseIntegrationTest
import com.rentalcarsystem.reservationservice.dtos.request.reservation.ReservationReqDTO
import com.rentalcarsystem.reservationservice.dtos.response.PagedResDTO
import com.rentalcarsystem.reservationservice.dtos.response.reservation.CustomerReservationResDTO
import com.rentalcarsystem.reservationservice.dtos.response.reservation.StaffReservationResDTO
import com.rentalcarsystem.reservationservice.enums.*
import com.rentalcarsystem.reservationservice.models.CarModel
import com.rentalcarsystem.reservationservice.models.Reservation
import com.rentalcarsystem.reservationservice.models.Vehicle
import com.rentalcarsystem.reservationservice.repositories.CarModelRepository
import com.rentalcarsystem.reservationservice.repositories.ReservationRepository
import com.rentalcarsystem.reservationservice.repositories.VehicleRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ReservationControllerTest : BaseIntegrationTest() {
    private val BASE_URl = "/api/v1/reservations"

    @Autowired
    private lateinit var reservationRepository: ReservationRepository

    @Autowired
    private lateinit var vehicleRepository: VehicleRepository

    @Autowired
    private lateinit var carModelRepository: CarModelRepository

    @Autowired
    lateinit var mockMvc: MockMvc

    // lateinit var objectMapper: ObjectMapper
    @Autowired
    val objectMapper = ObjectMapper().registerKotlinModule()

    final val carModel = CarModel(
        brand = "Volkswagen",
        model = "Polo",
        year = "2022",
        segment = CarSegment.COMPACT,
        doorsNumber = 4,
        seatingCapacity = 5,
        luggageCapacity = 100.0,
        category = CarCategory.ECONOMY,
        features = mutableSetOf(),
        engineType = EngineType.PETROL,
        transmissionType = TransmissionType.AUTOMATIC,
        drivetrain = Drivetrain.FWD,
        motorDisplacement = 4,
        rentalPrice = 50.0
    )

    final val vehicle = Vehicle(
        licensePlate = "EFG1234",
        vin = "3HGBH41JXMN109186",
        carModel = carModel,
        status = CarStatus.AVAILABLE,
        kmTravelled = 15000.0,
        pendingCleaning = false,
        pendingRepair = false
    )

    val reservation1 = Reservation(
        customerId = 1,
        vehicle = vehicle,
        creationDate = LocalDateTime.of(2025, 4, 1, 9, 0, 0),
        plannedPickUpDate = LocalDateTime.of(2025, 4, 3, 9, 0, 0),
        actualPickUpDate = LocalDateTime.of(2025, 4, 3, 10, 0, 0),
        plannedDropOffDate = LocalDateTime.of(2025, 4, 10, 10, 0, 0),
        actualDropOffDate = LocalDateTime.of(2025, 4, 10, 9, 45, 0),
        status = ReservationStatus.CONFIRMED,
        wasDeliveryLate = null,
        wasChargedFee = false,
        wasVehicleDamaged = false,
        wasInvolvedInAccident = false
    )

    private var createdReservationId: Long? = null

    @BeforeAll
    fun setup() {
        // Save car model first
        carModelRepository.save(carModel)

        // Save vehicle next so that it references the existing car model
        vehicleRepository.save(vehicle)

        // Save reservation next so that it reference the existing vehicle
        vehicle.addReservation(reservation1)

        reservationRepository.save(reservation1)
    }

    @AfterAll
    fun cleanup() {
        vehicle.removeReservation(reservation1)
        vehicleRepository.deleteAll()
        carModelRepository.deleteAll()
    }

    @Nested
    inner class ReservationSuccessTests {
        @Test
        @Order(1)
        fun `it should return all reservations successfully`() {
            val result = mockMvc.perform(MockMvcRequestBuilders.get(BASE_URl))
                .andExpect(status().isOk).andReturn()

            val content = result.response.contentAsString
            val typeRef = object : TypeReference<PagedResDTO<StaffReservationResDTO>>() {}
            val pagedReservations: PagedResDTO<StaffReservationResDTO> = objectMapper.readValue(content, typeRef)

            assertEquals(1, pagedReservations.content.size)
            assertEquals(reservation1.actualPickUpDate, pagedReservations.content.first().commonInfo.actualPickUpDate)
            assertEquals(reservation1.vehicle?.getId()!!, pagedReservations.content.first().commonInfo.vehicleId)
            assertEquals(reservation1.status, pagedReservations.content.first().commonInfo.status)
        }

        @Test
        @Order(2)
        fun `it should create a reservation`() {
            val input = ReservationReqDTO(
                customerId = 1L,
                carModelId = 1L,
                plannedPickUpDate = LocalDateTime.now(ZoneOffset.UTC).plusDays(1),
                plannedDropOffDate = LocalDateTime.now(ZoneOffset.UTC).plusDays(5)
            )

            val result = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/reservations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.carModelId").value(1))
                .andExpect(jsonPath("$.customerId").doesNotExist())
                .andReturn()

            val content = result.response.contentAsString
            val reservation: CustomerReservationResDTO =
                objectMapper.readValue(content, CustomerReservationResDTO::class.java)

            createdReservationId = reservation.id

            assertEquals(1L, reservation.carModelId)
            assertEquals("Toyota", reservation.brand)
        }

        @Nested
        inner class ErrorHandling {

            @Test
            fun `should return 422 for invalid car model id`() {
                val input = ReservationReqDTO(
                    customerId = 1L,
                    carModelId = -1L,  // Invalid car model id
                    plannedPickUpDate = LocalDateTime.now(ZoneOffset.UTC).plusDays(1),
                    plannedDropOffDate = LocalDateTime.now(ZoneOffset.UTC).plusDays(5)
                )

                mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                ).andExpect(status().isUnprocessableEntity)
            }

            @Test
            fun `should return 422 for invalid planned pick-up date`() {
                val input = ReservationReqDTO(
                    customerId = 1L,
                    carModelId = 1L,
                    plannedPickUpDate = LocalDateTime.now(ZoneOffset.UTC)
                        .minusDays(1),  // Invalid pick-up date in the past
                    plannedDropOffDate = LocalDateTime.now(ZoneOffset.UTC).plusDays(5)
                )

                mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                ).andExpect(status().isUnprocessableEntity)
            }
        }
    }

    @Nested
    inner class GetReservationTests {

        @Test
        fun `it should return a reservation by id`() {
            assertNotNull(createdReservationId, "Reservation ID should be initialized from the create test.")

            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/reservations/${createdReservationId!!}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(createdReservationId))
                .andExpect(jsonPath("$.carModelId").value(1))
        }
    }

    @Nested
    inner class UpdateReservationTests {

        @Test
        fun `it should update a reservation`() {
            val input = ReservationReqDTO(
                customerId = 1L,
                carModelId = 1L,
                plannedPickUpDate = LocalDateTime.now(ZoneOffset.UTC).plusDays(2),
                plannedDropOffDate = LocalDateTime.now(ZoneOffset.UTC).plusDays(6)
            )

            mockMvc.perform(
                MockMvcRequestBuilders.put("/api/v1/reservations/${createdReservationId!!}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$.carModelId").value(1))
        }

        @Nested
        inner class ErrorHandling {

            @Test
            fun `should return 404 when updating non-existing reservation`() {
                val input = ReservationReqDTO(
                    customerId = 1L,
                    carModelId = 1L,
                    plannedPickUpDate = LocalDateTime.now(ZoneOffset.UTC).plusDays(2),
                    plannedDropOffDate = LocalDateTime.now(ZoneOffset.UTC).plusDays(6)
                )

                mockMvc.perform(
                    MockMvcRequestBuilders.put("/api/v1/reservations/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                ).andExpect(status().isNotFound)
            }
        }
    }

    @Nested
    inner class DeleteReservationTests {

        @Test
        @Order(3)
        fun `it should delete a reservation`() {
            assertNotNull(createdReservationId, "Reservation ID should be initialized from the create test.")

            mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/reservations/${createdReservationId!!}"))
                .andExpect(status().isNoContent)
        }

        @Nested
        inner class ErrorHandling {

            @Test
            fun `should return 404 for non-existent reservation deletion`() {
                mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/reservations/99999"))
                    .andExpect(status().isNotFound)
            }
        }
    }
}

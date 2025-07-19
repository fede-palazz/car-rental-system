package com.rentalcarsystem.reservationservice.integration.endtoend

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rentalcarsystem.reservationservice.dtos.request.reservation.*
import com.rentalcarsystem.reservationservice.enums.*
import com.rentalcarsystem.reservationservice.models.CarModel
import com.rentalcarsystem.reservationservice.models.Vehicle
import com.rentalcarsystem.reservationservice.repositories.CarModelRepository
import com.rentalcarsystem.reservationservice.repositories.ReservationRepository
import com.rentalcarsystem.reservationservice.repositories.VehicleRepository
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReservationE2ETest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var reservationRepository: ReservationRepository

    @Autowired
    private lateinit var vehicleRepository: VehicleRepository

    @Autowired
    private lateinit var carModelRepository: CarModelRepository

    private val BASE_URL = "/api/v1/reservations"
    private var reservationId: Long? = null
    private var carModelId: Long? = null

    @BeforeEach
    fun setup() {
        reservationRepository.deleteAll()
        vehicleRepository.deleteAll()
        carModelRepository.deleteAll()
        objectMapper.registerKotlinModule()

        // Create prerequisite car model
        val carModel = carModelRepository.save(
            CarModel(
                brand = "Toyota",
                model = "Camry",
                year = "2023",
                segment = CarSegment.COMPACT,
                doorsNumber = 4,
                seatingCapacity = 5,
                luggageCapacity = 400.0,
                category = CarCategory.MIDSIZE,
                engineType = EngineType.HYBRID,
                transmissionType = TransmissionType.AUTOMATIC,
                drivetrain = Drivetrain.FWD,
                motorDisplacement = 2500,
                rentalPrice = 80.0
            )
        )
        carModelId = carModel.getId()

        // Create prerequisite vehicle
        vehicleRepository.save(
            Vehicle(
                licensePlate = "ABC123",
                vin = "1HGCM82633A123456",
                carModel = carModel,
                status = CarStatus.AVAILABLE,
                kmTravelled = 15000.0,
                pendingCleaning = false,
                pendingRepair = false
            )
        )
    }

    @Test
    fun `should perform complete reservation lifecycle end-to-end`() {
        // 1. Create reservation
        val createRequest = ReservationReqDTO(
            customerId = 1L,
            carModelId = carModelId!!,
            plannedPickUpDate = LocalDateTime.now().plusDays(1),
            plannedDropOffDate = LocalDateTime.now().plusDays(5)
        )

        val postResult = mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.carModelId").value(carModelId))
            .andExpect(jsonPath("$.brand").value("Toyota"))
            .andReturn()

        val responseJson = postResult.response.contentAsString
        reservationId = objectMapper.readTree(responseJson).get("id").asLong()

        // 2. Get reservation details
        mockMvc.perform(get("$BASE_URL/$reservationId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(reservationId))
            .andExpect(jsonPath("$.status").value("PENDING"))

        // 3. Set actual pick-up date
        val pickUpRequest = ActualPickUpDateReqDTO(
            actualPickUpDate = LocalDateTime.now()
        )

        mockMvc.perform(
            put("$BASE_URL/$reservationId/pickup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pickUpRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.actualPickUpDate").exists())
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))

        // 4. Finalize reservation
        val finalizeRequest = FinalizeReservationReqDTO(
            actualDropOffDate = LocalDateTime.now().plusDays(5),
            wasDeliveryLate = false,
            wasChargedFee = false,
            wasVehicleDamaged = false,
            wasInvolvedInAccident = false
        )

        mockMvc.perform(
            put("$BASE_URL/$reservationId/finalize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(finalizeRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.actualDropOffDate").exists())
            .andExpect(jsonPath("$.status").value("COMPLETED"))

        // 5. Delete reservation
        mockMvc.perform(delete("$BASE_URL/$reservationId"))
            .andExpect(status().isNoContent)

        // 6. Verify deletion
        mockMvc.perform(get("$BASE_URL/$reservationId"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should handle invalid operations appropriately`() {
        // 1. Test invalid create request (past pick-up date)
        val invalidCreate = ReservationReqDTO(
            customerId = 1L,
            carModelId = carModelId!!,
            plannedPickUpDate = LocalDateTime.now().minusDays(1),
            plannedDropOffDate = LocalDateTime.now().plusDays(5)
        )

        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCreate))
        )
            .andExpect(status().isUnprocessableEntity)

        // 2. Test non-existent reservation
        mockMvc.perform(get("$BASE_URL/99999"))
            .andExpect(status().isNotFound)

        // 3. Test invalid filters
        mockMvc.perform(
            get(BASE_URL)
                .param("customerId", "-1")
                .param("page", "-1")
                .param("size", "0")
        )
            .andExpect(status().isUnprocessableEntity)

        // 4. Test finalize non-existent reservation
        val finalizeRequest = FinalizeReservationReqDTO(
            actualDropOffDate = LocalDateTime.now(),
            wasDeliveryLate = false,
            wasChargedFee = false,
            wasVehicleDamaged = false,
            wasInvolvedInAccident = false
        )

        mockMvc.perform(
            put("$BASE_URL/99999/finalize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(finalizeRequest))
        )
            .andExpect(status().isNotFound)
    }
}
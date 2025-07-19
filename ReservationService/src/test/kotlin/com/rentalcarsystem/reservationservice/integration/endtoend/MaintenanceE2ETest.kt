package com.rentalcarsystem.reservationservice.integration.endtoend

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rentalcarsystem.reservationservice.dtos.request.MaintenanceReqDTO
import com.rentalcarsystem.reservationservice.enums.*
import com.rentalcarsystem.reservationservice.models.CarModel
import com.rentalcarsystem.reservationservice.models.Vehicle
import com.rentalcarsystem.reservationservice.repositories.CarModelRepository
import com.rentalcarsystem.reservationservice.repositories.MaintenanceRepository
import com.rentalcarsystem.reservationservice.repositories.VehicleRepository
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MaintenanceE2ETest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var maintenanceRepository: MaintenanceRepository

    @Autowired
    private lateinit var vehicleRepository: VehicleRepository

    @Autowired
    private lateinit var carModelRepository: CarModelRepository

    private val BASE_URL = "/api/v1/vehicles"
    private var vehicleId: Long? = null
    private var maintenanceId: Long? = null

    @BeforeEach
    fun setup() {
        maintenanceRepository.deleteAll()
        vehicleRepository.deleteAll()
        carModelRepository.deleteAll()
        objectMapper.registerKotlinModule()
    }

    @Test
    fun `should perform complete maintenance lifecycle end-to-end`() {
        // 1. Create prerequisite car model
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

        // 2. Create prerequisite vehicle
        val vehicle = vehicleRepository.save(
            Vehicle(
                licensePlate = "ABC123",
                vin = "1HGCM82633A123456",
                carModel = carModel,
                status = CarStatus.AVAILABLE,
                kmTravelled = 50000.0,
                pendingCleaning = false,
                pendingRepair = false
            )
        )
        vehicleId = vehicle.getId()

        // 3. Create maintenance record
        val createRequest = MaintenanceReqDTO(
            defects = "Oil change needed",
            type = "Routine",
            completed = false,
            upcomingServiceNeeds = "Brake inspection"
        )

        val postResult = mockMvc.perform(
            post("$BASE_URL/${vehicleId}/maintenances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.defects").value("Oil change needed"))
            .andExpect(jsonPath("$.type").value("Routine"))
            .andReturn()

        val responseJson = postResult.response.contentAsString
        maintenanceId = objectMapper.readTree(responseJson).get("id").asLong()

        // 4. Verify vehicle status changed
        mockMvc.perform(get("$BASE_URL/$vehicleId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("IN_MAINTENANCE"))

        // 5. Get maintenance record
        mockMvc.perform(get("$BASE_URL/$vehicleId/maintenances/$maintenanceId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.defects").value("Oil change needed"))
            .andExpect(jsonPath("$.completed").value(false))

        // 6. Update maintenance as completed
        val updateRequest = MaintenanceReqDTO(
            defects = "Oil changed",
            type = "Routine",
            completed = true,
            upcomingServiceNeeds = "None"
        )

        mockMvc.perform(
            put("$BASE_URL/$vehicleId/maintenances/$maintenanceId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.completed").value(true))
            .andExpect(jsonPath("$.defects").value("Oil changed"))

        // 7. Verify vehicle status returned to available
        mockMvc.perform(get("$BASE_URL/$vehicleId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("AVAILABLE"))

        // 8. Delete maintenance record
        mockMvc.perform(delete("$BASE_URL/$vehicleId/maintenances/$maintenanceId"))
            .andExpect(status().isNoContent)

        // 9. Verify deletion
        mockMvc.perform(get("$BASE_URL/$vehicleId/maintenances/$maintenanceId"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should handle invalid operations appropriately`() {
        // 1. Setup prerequisite data
        val carModel = carModelRepository.save(
            CarModel(
                brand = "Honda",
                model = "Civic",
                year = "2023",
                segment = CarSegment.COMPACT,
                doorsNumber = 4,
                seatingCapacity = 5,
                luggageCapacity = 350.0,
                category = CarCategory.ECONOMY,
                engineType = EngineType.PETROL,
                transmissionType = TransmissionType.AUTOMATIC,
                drivetrain = Drivetrain.FWD,
                motorDisplacement = 1800,
                rentalPrice = 60.0
            )
        )

        val vehicle = vehicleRepository.save(
            Vehicle(
                licensePlate = "XYZ789",
                vin = "2HGES16575H123456",
                carModel = carModel,
                status = CarStatus.AVAILABLE,
                kmTravelled = 30000.0,
                pendingCleaning = false,
                pendingRepair = false
            )
        )
        vehicleId = vehicle.getId()

        // 2. Test invalid create request
        val invalidCreate = MaintenanceReqDTO(
            defects = "", // Invalid empty defects
            type = "", // Invalid empty type
            completed = true, // Cannot create completed maintenance
            upcomingServiceNeeds = null
        )

        mockMvc.perform(
            post("$BASE_URL/$vehicleId/maintenances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCreate))
        )
            .andExpect(status().isUnprocessableEntity)

        // 3. Test non-existent vehicle ID
        mockMvc.perform(
            post("$BASE_URL/99999/maintenances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(MaintenanceReqDTO(
                    defects = "Test",
                    type = "Test",
                    completed = false,
                    upcomingServiceNeeds = null
                )))
        )
            .andExpect(status().isNotFound)

        // 4. Test non-existent maintenance ID
        mockMvc.perform(get("$BASE_URL/$vehicleId/maintenances/99999"))
            .andExpect(status().isNotFound)

        // 5. Test invalid vehicle-maintenance combination
        mockMvc.perform(get("$BASE_URL/99999/maintenances/99999"))
            .andExpect(status().isNotFound)
    }
}
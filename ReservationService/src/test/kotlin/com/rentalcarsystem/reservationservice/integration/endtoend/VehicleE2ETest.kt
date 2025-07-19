package com.rentalcarsystem.reservationservice.integration.endtoend

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rentalcarsystem.reservationservice.dtos.request.VehicleReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.VehicleUpdateReqDTO
import com.rentalcarsystem.reservationservice.enums.*
import com.rentalcarsystem.reservationservice.models.CarModel
import com.rentalcarsystem.reservationservice.repositories.CarModelRepository
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
class VehicleE2ETest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var vehicleRepository: VehicleRepository

    @Autowired
    private lateinit var carModelRepository: CarModelRepository

    private val BASE_URL = "/api/v1/vehicles"
    private var vehicleId: Long? = null
    private var carModelId: Long? = null

    @BeforeEach
    fun setup() {
        vehicleRepository.deleteAll()
        carModelRepository.deleteAll()
        objectMapper.registerKotlinModule()
    }

    @Test
    fun `should perform complete vehicle lifecycle end-to-end`() {
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
        carModelId = carModel.getId()

        // 2. Create vehicle
        val createRequest = VehicleReqDTO(
            licensePlate = "ABC123",
            vin = "1HGCM82633A123456",
            status = CarStatus.AVAILABLE,
            kmTravelled = 15000.0,
            pendingCleaning = false,
            pendingRepair = false,
            carModelId = carModelId!!
        )

        val postResult = mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.licensePlate").value("ABC123"))
            .andExpect(jsonPath("$.brand").value("Toyota"))
            .andReturn()

        val responseJson = postResult.response.contentAsString
        vehicleId = objectMapper.readTree(responseJson).get("id").asLong()

        // 3. Get vehicle details
        mockMvc.perform(get("$BASE_URL/$vehicleId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.licensePlate").value("ABC123"))
            .andExpect(jsonPath("$.vin").value("1HGCM82633A123456"))
            .andExpect(jsonPath("$.brand").value("Toyota"))
            .andExpect(jsonPath("$.model").value("Camry"))

        // 4. Get vehicles with filter
        mockMvc.perform(
            get(BASE_URL)
                .param("page", "0")
                .param("size", "10")
                .param("brand", "Toyota")
                .param("licensePlate", "ABC")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].brand").value("Toyota"))
            .andExpect(jsonPath("$.content[0].licensePlate").value("ABC123"))

        // 5. Update vehicle
        val updateRequest = VehicleUpdateReqDTO(
            licensePlate = "XYZ789",
            status = CarStatus.IN_MAINTENANCE,
            kmTravelled = 16000.0,
            pendingCleaning = true,
            pendingRepair = true
        )

        mockMvc.perform(
            put("$BASE_URL/$vehicleId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.licensePlate").value("XYZ789"))
            .andExpect(jsonPath("$.status").value("IN_MAINTENANCE"))
            .andExpect(jsonPath("$.kmTravelled").value(16000.0))
            .andExpect(jsonPath("$.pendingCleaning").value(true))
            .andExpect(jsonPath("$.pendingRepair").value(true))

        // 6. Delete vehicle
        mockMvc.perform(delete("$BASE_URL/$vehicleId"))
            .andExpect(status().isNoContent)

        // 7. Verify deletion
        mockMvc.perform(get("$BASE_URL/$vehicleId"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should handle invalid operations appropriately`() {
        // 1. Create prerequisite car model
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
        carModelId = carModel.getId()

        // 2. Test invalid create request
        val invalidCreate = VehicleReqDTO(
            licensePlate = "AB", // Invalid license plate length
            vin = "", // Invalid empty VIN
            status = CarStatus.AVAILABLE,
            kmTravelled = -100.0, // Invalid negative km
            pendingCleaning = false,
            pendingRepair = false,
            carModelId = carModelId!!
        )

        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCreate))
        )
            .andExpect(status().isUnprocessableEntity)

        // 3. Test non-existent vehicle ID
        mockMvc.perform(get("$BASE_URL/99999"))
            .andExpect(status().isNotFound)

        // 4. Test invalid pagination parameters
        mockMvc.perform(
            get(BASE_URL)
                .param("page", "-1")
                .param("size", "0")
        )
            .andExpect(status().isUnprocessableEntity)

        // 5. Test invalid sort parameters
        mockMvc.perform(
            get(BASE_URL)
                .param("sort", "invalidField")
                .param("order", "invalid")
        )
            .andExpect(status().isUnprocessableEntity)

        // 6. Test update with non-existent ID
        val updateRequest = VehicleUpdateReqDTO(
            licensePlate = "TEST999",
            status = CarStatus.AVAILABLE,
            kmTravelled = 1000.0,
            pendingCleaning = false,
            pendingRepair = false
        )

        mockMvc.perform(
            put("$BASE_URL/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isNotFound)
    }
}
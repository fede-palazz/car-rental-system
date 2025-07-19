package com.rentalcarsystem.reservationservice.integration.endtoend

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rentalcarsystem.reservationservice.dtos.request.CarModelReqDTO
import com.rentalcarsystem.reservationservice.enums.*
import com.rentalcarsystem.reservationservice.models.CarFeature
import com.rentalcarsystem.reservationservice.repositories.CarFeatureRepository
import com.rentalcarsystem.reservationservice.repositories.CarModelRepository
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
class CarModelE2ETest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var carModelRepository: CarModelRepository

    @Autowired
    private lateinit var carFeatureRepository: CarFeatureRepository

    private val BASE_URL = "/api/v1/models"
    private var createdModelId: Long? = null
    private var createdFeatureId: Long? = null

    @BeforeEach
    fun setup() {
        carModelRepository.deleteAll()
        carFeatureRepository.deleteAll()
        objectMapper.registerKotlinModule()
    }

    @Test
    fun `should perform complete car model lifecycle end-to-end`() {
        // 1. Create a feature first
        val feature = CarFeature(description = "GPS Navigation")
        val savedFeature = carFeatureRepository.save(feature)
        createdFeatureId = savedFeature.getId()

        // 2. Create car model
        val createRequest = CarModelReqDTO(
            brand = "Tesla",
            model = "Model 3",
            year = "2023",
            segment = CarSegment.COMPACT,
            doorsNumber = 4,
            seatingCapacity = 5,
            luggageCapacity = 425.0,
            category = CarCategory.LUXURY,
            engineType = EngineType.ELECTRIC,
            transmissionType = TransmissionType.AUTOMATIC,
            drivetrain = Drivetrain.AWD,
            motorDisplacement = 0,
            rentalPrice = 100.0,
            featureIds = listOf(createdFeatureId!!)
        )

        val postResult = mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.brand").value("Tesla"))
            .andExpect(jsonPath("$.model").value("Model 3"))
            .andReturn()

        val responseJson = postResult.response.contentAsString
        createdModelId = objectMapper.readTree(responseJson).get("id").asLong()

        // 3. Fetch created car model
        mockMvc.perform(get("$BASE_URL/$createdModelId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.brand").value("Tesla"))
            .andExpect(jsonPath("$.engineType").value("ELECTRIC"))
            .andExpect(jsonPath("$.features").isArray)
            .andExpect(jsonPath("$.features[0].description").value("GPS Navigation"))

        // 4. Get car model features
        mockMvc.perform(get("$BASE_URL/$createdModelId/features"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].description").value("GPS Navigation"))

        // 5. Update car model
        val updateRequest = CarModelReqDTO(
            brand = "Tesla",
            model = "Model 3 Performance",
            year = "2023",
            segment = CarSegment.COMPACT,
            doorsNumber = 4,
            seatingCapacity = 5,
            luggageCapacity = 425.0,
            category = CarCategory.LUXURY,
            engineType = EngineType.ELECTRIC,
            transmissionType = TransmissionType.AUTOMATIC,
            drivetrain = Drivetrain.AWD,
            motorDisplacement = 0,
            rentalPrice = 120.0,
            featureIds = listOf(createdFeatureId!!)
        )

        mockMvc.perform(
            put("$BASE_URL/$createdModelId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.model").value("Model 3 Performance"))
            .andExpect(jsonPath("$.rentalPrice").value(120.0))

        // 6. Filter car models
        mockMvc.perform(
            get(BASE_URL)
                .param("page", "0")
                .param("size", "10")
                .param("brand", "Tesla")
                .param("category", "LUXURY")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].brand").value("Tesla"))
            .andExpect(jsonPath("$.content[0].category").value("LUXURY"))

        // 7. Delete car model
        mockMvc.perform(delete("$BASE_URL/$createdModelId"))
            .andExpect(status().isNoContent)

        // 8. Verify deletion
        mockMvc.perform(get("$BASE_URL/$createdModelId"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should handle invalid operations appropriately`() {
        // Test invalid create request
        val invalidCreate = CarModelReqDTO(
            brand = "", // Invalid empty brand
            model = "Test",
            year = "2023",
            segment = CarSegment.COMPACT,
            doorsNumber = -1, // Invalid negative doors
            seatingCapacity = 5,
            luggageCapacity = 100.0,
            category = CarCategory.ECONOMY,
            engineType = EngineType.PETROL,
            transmissionType = TransmissionType.MANUAL,
            drivetrain = Drivetrain.FWD,
            motorDisplacement = 2,
            rentalPrice = -50.0, // Invalid negative price
            featureIds = listOf()
        )

        mockMvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCreate))
        )
            .andExpect(status().isUnprocessableEntity)

        // Test non-existent ID
        mockMvc.perform(get("$BASE_URL/999999"))
            .andExpect(status().isNotFound)

        // Test invalid sorting
        mockMvc.perform(
            get(BASE_URL)
                .param("sort", "invalidField")
                .param("order", "invalid")
        )
            .andExpect(status().isUnprocessableEntity)

        // Test invalid pagination
        mockMvc.perform(
            get(BASE_URL)
                .param("page", "-1")
                .param("size", "0")
        )
            .andExpect(status().isUnprocessableEntity)
    }
}
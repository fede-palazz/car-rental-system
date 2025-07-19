package com.rentalcarsystem.reservationservice.integration.controllers

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rentalcarsystem.reservationservice.integration.BaseIntegrationTest
import com.rentalcarsystem.reservationservice.dtos.request.CarModelReqDTO
import com.rentalcarsystem.reservationservice.dtos.response.CarModelResDTO
import com.rentalcarsystem.reservationservice.dtos.response.PagedResDTO
import com.rentalcarsystem.reservationservice.enums.*
import com.rentalcarsystem.reservationservice.models.CarFeature
import com.rentalcarsystem.reservationservice.models.CarModel
import com.rentalcarsystem.reservationservice.repositories.CarFeatureRepository
import com.rentalcarsystem.reservationservice.repositories.CarModelRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class CarModelControllerTest : BaseIntegrationTest() {
    private val BASE_URL = "/api/v1/models"

    @Autowired
    private lateinit var carModelRepository: CarModelRepository

    @Autowired
    private lateinit var carFeatureRepository: CarFeatureRepository

    @Autowired
    lateinit var mockMvc: MockMvc

    // lateinit var objectMapper: ObjectMapper
    @Autowired
    val objectMapper = ObjectMapper().registerKotlinModule()

    final val feature1Model1 = CarFeature(description = "Feature 1 of Model 1")
    final val feature2Model1 = CarFeature(description = "Feature 2 of Model 1")
    final val feature1Model2 = CarFeature(description = "Feature 1 of Model 2")
    final val feature2Model2 = CarFeature(description = "Feature 2 of Model 2")

    val features: List<CarFeature> = mutableListOf(
        feature1Model1,
        feature2Model1,
        feature1Model2,
        feature2Model2
    )

    val carModels: List<CarModel> = mutableListOf(
        CarModel(
            brand = "Toyota",
            model = "Corolla",
            year = "2022",
            segment = CarSegment.COMPACT,
            doorsNumber = 4,
            seatingCapacity = 5,
            luggageCapacity = 100.0,
            category = CarCategory.ECONOMY,
            features = mutableSetOf(
                feature1Model1,
                feature2Model1
            ),
            engineType = EngineType.ELECTRIC,
            transmissionType = TransmissionType.AUTOMATIC,
            drivetrain = Drivetrain.FWD,
            motorDisplacement = 4,
            rentalPrice = 50.0
        ),
        CarModel(
            brand = "Renault",
            model = "Twingo",
            year = "2020",
            segment = CarSegment.COMPACT,
            doorsNumber = 4,
            seatingCapacity = 5,
            luggageCapacity = 80.0,
            category = CarCategory.ECONOMY,
            features = mutableSetOf(
                feature1Model2,
                feature2Model2
            ),
            engineType = EngineType.ELECTRIC,
            transmissionType = TransmissionType.AUTOMATIC,
            drivetrain = Drivetrain.FWD,
            motorDisplacement = 4,
            rentalPrice = 50.0
        )
    )

    @BeforeAll
    fun setup() {
        // Save features first
        carFeatureRepository.saveAll(features)

        // Save car models next so that they reference the existing features
        carModelRepository.saveAll(carModels)
    }

    @AfterAll
    fun cleanup() {
        // Delete all car models and features
        carModelRepository.deleteAll()
        carFeatureRepository.deleteAll()
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation::class)
    inner class CarModelSuccessTests {
        @Test
        @Order(1)
        fun `it should get all the car models`() {
            val result = mockMvc.perform(
                MockMvcRequestBuilders.get(BASE_URL)
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "brand")
                    .param("order", "asc")
            ).andExpect(status().isOk).andReturn()

            val content = result.response.contentAsString
            val typeRef = object: TypeReference<PagedResDTO<CarModelResDTO>>() {}
            val pagedCarModels: PagedResDTO<CarModelResDTO> = objectMapper.readValue(content, typeRef)

            assertEquals(2, pagedCarModels.totalElements)
            assertEquals("Renault", pagedCarModels.content.first().brand)
        }

        @Test
        @Order(2)
        fun `it should get car model by id`() {
            val result = mockMvc.perform(MockMvcRequestBuilders.get("$BASE_URL/2"))
                .andExpect(status().isOk).andReturn()

            val content = result.response.contentAsString
            val typeRef = object: TypeReference<CarModelResDTO>() {}
            val carModel: CarModelResDTO = objectMapper.readValue(content, typeRef)

            assertEquals(CarCategory.ECONOMY, carModel.category)
        }

        @Test
        @Order(3)
        fun `it should get all car features`() {
            val result = mockMvc.perform(MockMvcRequestBuilders.get("$BASE_URL/features"))
                .andExpect(status().isOk).andReturn()

            val content = result.response.contentAsString
            val typeRef = object: TypeReference<List<CarFeature>>() {}
            val features: List<CarFeature> = objectMapper.readValue(content, typeRef)

            assertEquals(4, features.size)
        }

        @Test
        @Order(4)
        fun `it should get car feature by id`() {
            val validId = 3L
            val result = mockMvc.perform(MockMvcRequestBuilders.get("$BASE_URL/features/$validId"))
                .andExpect(status().isOk).andReturn()

            val content = result.response.contentAsString
            val typeRef = object: TypeReference<CarFeature>() {}
            val feature: CarFeature = objectMapper.readValue(content, typeRef)

            assertEquals(validId, feature.getId()!!)
            assertEquals("Feature 1 of Model 2", feature.description)
        }

        @Test
        @Order(5)
        fun `it should get features by car model id`() {
            val result = mockMvc.perform(MockMvcRequestBuilders.get("$BASE_URL/2/features"))
                .andExpect(status().isOk).andReturn()

            val content = result.response.contentAsString
            val typeRef = object: TypeReference<List<CarFeature>>() {}
            val features: List<CarFeature> = objectMapper.readValue(content, typeRef)

            assertEquals(2, features.size)
        }

        @Test
        @Order(6)
        fun `it should create car model`() {
            val input = CarModelReqDTO(
                brand = "Renault",
                model = "Clio",
                year = "2020",
                segment = CarSegment.COMPACT,
                doorsNumber = 4,
                seatingCapacity = 5,
                luggageCapacity = 45.0,
                category = CarCategory.ECONOMY,
                featureIds = emptyList(),
                engineType = EngineType.PETROL,
                transmissionType = TransmissionType.AUTOMATIC,
                drivetrain = Drivetrain.FWD,
                motorDisplacement = 3,
                rentalPrice = 45.0
            )

            // Counting the number of car models before creating a new one
            val countBefore = carModelRepository.count()

            val result = mockMvc.perform(
                MockMvcRequestBuilders.post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input))
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andReturn()

            // Counting the number of car models after creating a new one
            val countAfter = carModelRepository.count()

            assertEquals(countAfter, countBefore + 1)

            val content = result.response.contentAsString
            val typeRef = object: TypeReference<CarModelResDTO>() {}
            val carModel: CarModelResDTO = objectMapper.readValue(content, typeRef)

            assertEquals("Renault", carModel.brand)
            assertEquals("Clio", carModel.model)
            assertEquals(Drivetrain.FWD, carModel.drivetrain)
        }

        @Test
        @Order(7)
        fun `it should update car model`() {
            val input = CarModelReqDTO(
                brand = "Updated",
                model = "Model",
                year = "2023",
                segment = CarSegment.COMPACT,
                doorsNumber = 4,
                seatingCapacity = 5,
                luggageCapacity = 50.0,
                category = CarCategory.ECONOMY,
                featureIds = emptyList(),
                engineType = EngineType.PETROL,
                transmissionType = TransmissionType.MANUAL,
                drivetrain = Drivetrain.FWD,
                motorDisplacement = 4,
                rentalPrice = 50.0
            )

            // Counting the number of car models before updating an existing one
            val countBefore = carModelRepository.count()

            val result = mockMvc.perform(
                MockMvcRequestBuilders.put("$BASE_URL/2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input))
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andReturn()

            // Counting the number of car models after updating an existing one
            val countAfter = carModelRepository.count()

            assertEquals(countAfter, countBefore)

            val content = result.response.contentAsString
            val typeRef = object: TypeReference<CarModelResDTO>() {}
            val carModel: CarModelResDTO = objectMapper.readValue(content, typeRef)

            assertEquals("Updated", carModel.brand)
            assertEquals("Model", carModel.model)
            assertEquals(TransmissionType.MANUAL, carModel.transmissionType)
        }

        @Test
        @Order(8)
        fun `it should delete car model`() {
            // Counting the number of car models before deleting an existing one
            val countBefore = carModelRepository.count()

            mockMvc.perform(MockMvcRequestBuilders.delete("$BASE_URL/2"))
                .andExpect(status().isNoContent)

            // Counting the number of car models after deleting an existing one
            val countAfter = carModelRepository.count()

            assertEquals(countAfter, countBefore - 1)
        }
    }

    @Nested
    inner class CarModelErrorTests {
        @Test
        fun `it should return 422 for invalid car model id on get`() {
            mockMvc.perform(MockMvcRequestBuilders.get("$BASE_URL/-1"))
                .andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `it should return 404 for non-existent car model`() {
            mockMvc.perform(MockMvcRequestBuilders.get("$BASE_URL/9999"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `it should return 422 for invalid sort parameter`() {
            mockMvc.perform(
                MockMvcRequestBuilders.get(BASE_URL)
                    .param("sort", "invalidField")
                    .param("order", "asc")
            ).andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `it should return 422 for invalid order parameter`() {
            mockMvc.perform(
                MockMvcRequestBuilders.get(BASE_URL)
                    .param("sort", "brand")
                    .param("order", "invalid")
            ).andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `it should return 422 for invalid page or size`() {
            mockMvc.perform(
                MockMvcRequestBuilders.get(BASE_URL)
                    .param("page", "-1")
                    .param("size", "0")
            ).andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `it should return 422 for invalid car feature id`() {
            mockMvc.perform(MockMvcRequestBuilders.get("$BASE_URL/features/-10"))
                .andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `it should return 404 for non-existent car feature`() {
            mockMvc.perform(MockMvcRequestBuilders.get("$BASE_URL/features/9999"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `it should return 422 for invalid car model id when getting features`() {
            mockMvc.perform(MockMvcRequestBuilders.get("$BASE_URL/-5/features"))
                .andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `it should return 404 for features of non-existent car model`() {
            mockMvc.perform(MockMvcRequestBuilders.get("$BASE_URL/9999/features"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `it should return 422 for invalid id on update`() {
            val input = CarModelReqDTO(
                brand = "Invalid",
                model = "Model",
                year = "2021",
                segment = CarSegment.COMPACT,
                doorsNumber = 4,
                seatingCapacity = 5,
                luggageCapacity = 45.0,
                category = CarCategory.ECONOMY,
                featureIds = emptyList(),
                engineType = EngineType.ELECTRIC,
                transmissionType = TransmissionType.AUTOMATIC,
                drivetrain = Drivetrain.FWD,
                motorDisplacement = 2,
                rentalPrice = 50.0
            )

            mockMvc.perform(
                MockMvcRequestBuilders.put("$BASE_URL/-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `it should return 404 when updating non-existent model`() {
            val input = CarModelReqDTO(
                brand = "Ghost",
                model = "Phantom",
                year = "2021",
                segment = CarSegment.COMPACT,
                doorsNumber = 4,
                seatingCapacity = 5,
                luggageCapacity = 45.0,
                category = CarCategory.ECONOMY,
                featureIds = emptyList(),
                engineType = EngineType.ELECTRIC,
                transmissionType = TransmissionType.AUTOMATIC,
                drivetrain = Drivetrain.FWD,
                motorDisplacement = 2,
                rentalPrice = 50.0
            )

            mockMvc.perform(
                MockMvcRequestBuilders.put("$BASE_URL/9999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().isNotFound)
        }

        @Test
        fun `it should return 422 when deleting with invalid id`() {
            mockMvc.perform(MockMvcRequestBuilders.delete("$BASE_URL/-1"))
                .andExpect(status().isUnprocessableEntity())
        }

        @Test
        fun `it should return 404 when deleting non-existent model`() {
            mockMvc.perform(MockMvcRequestBuilders.delete("$BASE_URL/9999"))
                .andExpect(status().isNotFound)
        }
    }
}

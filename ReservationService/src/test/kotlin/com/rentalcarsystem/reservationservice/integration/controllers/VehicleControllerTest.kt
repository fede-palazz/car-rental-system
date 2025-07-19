package com.rentalcarsystem.reservationservice.integration.controllers

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import com.rentalcarsystem.reservationservice.integration.BaseIntegrationTest
import com.rentalcarsystem.reservationservice.dtos.request.VehicleReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.VehicleUpdateReqDTO
import com.rentalcarsystem.reservationservice.dtos.response.PagedResDTO
import com.rentalcarsystem.reservationservice.dtos.response.VehicleResDTO
import com.rentalcarsystem.reservationservice.enums.*
import com.rentalcarsystem.reservationservice.models.CarModel
import com.rentalcarsystem.reservationservice.models.Vehicle
import com.rentalcarsystem.reservationservice.repositories.CarModelRepository
import com.rentalcarsystem.reservationservice.repositories.VehicleRepository
import org.junit.jupiter.api.*
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
class VehicleControllerTest : BaseIntegrationTest() {
    private val BASE_URl = "/api/v1/vehicles"

    @Autowired
    private lateinit var vehicleRepository: VehicleRepository

    @Autowired
    private lateinit var carModelRepository: CarModelRepository

    @Autowired
    lateinit var mockMvc: MockMvc

    // lateinit var objectMapper: ObjectMapper
    @Autowired
    val objectMapper = ObjectMapper().registerKotlinModule()


    val carModel = CarModel(
    brand = "Toyota",
    model = "Yaris",
    year = "2022",
    segment = CarSegment.COMPACT,
    doorsNumber = 4,
    seatingCapacity = 5,
    luggageCapacity = 100.0,
    category = CarCategory.ECONOMY,
    features = mutableSetOf(),
    engineType = EngineType.ELECTRIC,
    transmissionType = TransmissionType.AUTOMATIC,
    drivetrain = Drivetrain.FWD,
    motorDisplacement = 4,
    rentalPrice = 50.0
    )

    val vehicle = Vehicle(
    licensePlate = "XYZ1234",
    vin = "1HGBH41JXMN109186",
    carModel = carModel,
    status = CarStatus.AVAILABLE,
    kmTravelled = 15000.0,
    pendingCleaning = false,
    pendingRepair = false
    )
    @BeforeAll
    fun setup() {
        // Save the car model first
        carModelRepository.save(carModel)

        // Save vehicle next so that it references the existing car model
        vehicleRepository.save(vehicle)
    }

    @AfterAll
    fun cleanup() {
        vehicleRepository.deleteAll()
        carModelRepository.deleteAll()
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation::class)
    inner class VehicleSuccessTests {
        @Test
        @Order(1)
        fun `should return all vehicles successfully`() {
            val result = mockMvc.perform(
                MockMvcRequestBuilders.get(BASE_URl)
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "vin")
                    .param("order", "asc")
                ).andExpect(status().isOk).andReturn()

            val content = result.response.contentAsString
            val typeRef = object: TypeReference<PagedResDTO<VehicleResDTO>>() {}
            val pagedVehicles: PagedResDTO<VehicleResDTO> = objectMapper.readValue(content, typeRef)

            assertEquals(1, pagedVehicles.totalElements)
            assertEquals("1HGBH41JXMN109186", pagedVehicles.content.first().vin)
        }

        @Test
        @Order(2)
        fun `should return vehicle by id successfully`() {
            val result = mockMvc.perform(MockMvcRequestBuilders.get("$BASE_URl/1"))
                .andExpect(status().isOk).andReturn()

            val content = result.response.contentAsString
            val typeRef = object: TypeReference<VehicleResDTO>() {}
            val vehicle: VehicleResDTO = objectMapper.readValue(content, typeRef)

            assertEquals("XYZ1234", vehicle.licensePlate)
            assertEquals("Yaris", vehicle.model)
        }

        @Test
        @Order(3)
        fun `should create a new vehicle successfully`() {
            val vehicleReqDTO = VehicleReqDTO(
                vin = "JH4KA4650MC001234",
                licensePlate = "TEST234",
                status = CarStatus.AVAILABLE,
                kmTravelled = 10000.0,
                pendingCleaning = false,
                pendingRepair = false,
                carModelId = carModelRepository.findByModel(carModel.model).first().getId()!!
            )

            // Counting the number of vehicles before creating a new one
            val countBefore = vehicleRepository.count()

            val result = mockMvc.perform(
                MockMvcRequestBuilders.post(BASE_URl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(vehicleReqDTO))
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andReturn()

            // Counting the number of vehicles after creating a new one
            val countAfter = vehicleRepository.count()

            assertEquals(countAfter, countBefore + 1)

            val content = result.response.contentAsString
            val typeRef = object: TypeReference<VehicleResDTO>() {}
            val vehicle: VehicleResDTO = objectMapper.readValue(content, typeRef)

            assertEquals("TEST234", vehicle.licensePlate)
            assertEquals("Yaris", vehicle.model)
            assertEquals(CarStatus.AVAILABLE, vehicle.status)
        }

        @Test
        @Order(4)
        fun `should update vehicle successfully`() {
            val vehicleUpdateReqDTO = VehicleUpdateReqDTO(
                licensePlate = "NEW1234",
                status = CarStatus.AVAILABLE,
                kmTravelled = 16000.0,
                pendingCleaning = true,
                pendingRepair = true
            )

            // Counting the number of vehicles before updating one
            val countBefore = vehicleRepository.count()

            val result = mockMvc.perform(
                MockMvcRequestBuilders.put("$BASE_URl/2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(vehicleUpdateReqDTO))
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andReturn()

            // Counting the number of vehicles after updating one
            val countAfter = vehicleRepository.count()

            assertEquals(countAfter, countBefore)

            val content = result.response.contentAsString
            val typeRef = object: TypeReference<VehicleResDTO>() {}
            val vehicle: VehicleResDTO = objectMapper.readValue(content, typeRef)

            assertEquals(vehicleUpdateReqDTO.licensePlate, vehicle.licensePlate)
            assertEquals(vehicleUpdateReqDTO.status, vehicle.status)
            assertEquals(vehicleUpdateReqDTO.kmTravelled, vehicle.kmTravelled)
            assertEquals(vehicleUpdateReqDTO.pendingCleaning, vehicle.pendingCleaning)
            assertEquals(vehicleUpdateReqDTO.pendingRepair, vehicle.pendingRepair)
        }

        @Test
        @Order(5)
        fun `should delete vehicle successfully`() {
            // Counting the number of vehicles before deleting one
            val countBefore = vehicleRepository.count()

            mockMvc.perform(MockMvcRequestBuilders.delete("$BASE_URl/2"))
                .andExpect(status().isNoContent)

            // Counting the number of vehicles after deleting one
            val countAfter = vehicleRepository.count()

            assertEquals(countAfter, countBefore - 1)
        }
    }

    @Nested
    inner class VehicleErrorTests {
        @Test
        fun `should return 422 when sort field is invalid`() {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URl)
                .param("page", "0")
                .param("size", "10")
                .param("sort", "invalidField")
                .param("order", "asc"))
                .andExpect(status().isUnprocessableEntity)
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Parameter 'sort' invalid")))
        }

        @Test
        fun `should return 422 for invalid vehicle id`() {
            mockMvc.perform(MockMvcRequestBuilders.get("$BASE_URl/-5"))
                .andExpect(status().isUnprocessableEntity)
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid vehicle id")))
        }

        @Test
        fun `should return 404 when vehicle not found`() {
            mockMvc.perform(MockMvcRequestBuilders.get("$BASE_URl/999"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `should return 422 for invalid license plate`() {
            val invalidDTO = VehicleReqDTO(
                vin = "JH4KA4650MC001234",
                licensePlate = "ABC",
                status = CarStatus.AVAILABLE,
                kmTravelled = 10000.0,
                pendingCleaning = false,
                pendingRepair = false,
                carModelId = 1L
            )

            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isUnprocessableEntity) }

        @Test
        fun `should return 404 when updating non-existing vehicle`() {
            val dto = VehicleUpdateReqDTO(
                licensePlate = "XYZ1234",
                status = CarStatus.AVAILABLE,
                kmTravelled = 1234.0,
                pendingCleaning = false,
                pendingRepair = false
            )

            mockMvc.perform(MockMvcRequestBuilders.put("$BASE_URl/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `should return 404 when deleting non-existing vehicle`() {
            mockMvc.perform(MockMvcRequestBuilders.delete("$BASE_URl/999"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `should return 422 when deleting with invalid id`() {
            mockMvc.perform(MockMvcRequestBuilders.delete("$BASE_URl/-10"))
                .andExpect(status().isUnprocessableEntity)
        }
    }
}

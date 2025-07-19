package com.rentalcarsystem.reservationservice.integration.controllers

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import com.rentalcarsystem.reservationservice.integration.BaseIntegrationTest
import com.rentalcarsystem.reservationservice.dtos.request.MaintenanceReqDTO
import com.rentalcarsystem.reservationservice.dtos.response.MaintenanceResDTO
import com.rentalcarsystem.reservationservice.dtos.response.PagedResDTO
import com.rentalcarsystem.reservationservice.enums.*
import com.rentalcarsystem.reservationservice.models.CarModel
import com.rentalcarsystem.reservationservice.models.Maintenance
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class MaintenanceControllerTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var maintenanceRepository: MaintenanceRepository

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
        brand = "Toyota",
        model = "Camry",
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

    final val vehicle = Vehicle(
        licensePlate = "ABC1234",
        vin = "2HGBH41JXMN109186",
        carModel = carModel,
        status = CarStatus.AVAILABLE,
        kmTravelled = 15000.0,
        pendingCleaning = false,
        pendingRepair = false
    )

    val maintenance1 = Maintenance(
        vehicle = vehicle,
        defects = "Oil change",
        type = "Routine",
        completed = false,
        upcomingServiceNeeds = "Tire rotation",
        date = LocalDateTime.of(2025, 5, 1, 0, 0)
    )

    val maintenance2 = Maintenance(
        vehicle = vehicle,
        defects = "Brake pads broken",
        type = "Emergency",
        completed = true,
        upcomingServiceNeeds = "Brake pads change",
        date = LocalDateTime.of(2025, 6, 7, 0, 0)
    )
    @BeforeAll
    fun setup() {
        // Save car model first
        carModelRepository.save(carModel)

        // Save vehicle next so that it references the existing car model
        vehicleRepository.save(vehicle)

        // Save maintenances next so that they reference the existing vehicle
        vehicle.addMaintenance(maintenance1)
        vehicle.addMaintenance(maintenance2)

        maintenanceRepository.save(maintenance1)
        maintenanceRepository.save(maintenance2)
    }

    @AfterAll
    fun cleanup() {
        vehicle.removeMaintenance(maintenance1)
        vehicle.removeMaintenance(maintenance2)
        vehicleRepository.deleteAll()
        carModelRepository.deleteAll()
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation::class)
    inner class MaintenanceSuccessTests {
        @Test
        @Order(1)
        fun `should return all maintenances of a vehicle successfully`() {
            val vehicleId = vehicleRepository.getVehicleByLicensePlate(vehicle.licensePlate).first().getId()!!
            val result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/vehicles/$vehicleId/maintenances"))
                .andExpect(status().isOk).andReturn()

            val content = result.response.contentAsString
            val typeRef = object: TypeReference<PagedResDTO<MaintenanceResDTO>>() {}
            val pagedMaintenances: PagedResDTO<MaintenanceResDTO> = objectMapper.readValue(content, typeRef)

            assertEquals(2, pagedMaintenances.content.size)
            assertEquals(maintenance2.type, pagedMaintenances.content.first().type)
        }

        @Test
        @Order(2)
        fun `should return a maintenance by id successfully`() {
            val vehicleId = vehicleRepository.getVehicleByLicensePlate(vehicle.licensePlate).first().getId()!!
            val result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/vehicles/$vehicleId/maintenances/2"))
                .andExpect(status().isOk).andReturn()

            val content = result.response.contentAsString
            val typeRef = object: TypeReference<MaintenanceResDTO>() {}
            val maintenance: MaintenanceResDTO = objectMapper.readValue(content, typeRef)

            assertEquals(maintenance2.type, maintenance.type)
            assertEquals(maintenance2.upcomingServiceNeeds, maintenance.upcomingServiceNeeds)
            assertEquals(maintenance2.defects, maintenance.defects)
            assertEquals(maintenance2.completed, maintenance.completed)
            assertEquals(maintenance2.date, maintenance.date)
        }

        @Test
        @Order(3)
        fun `should create a maintenance record for a vehicle`() {
            val input = MaintenanceReqDTO(
                defects = "Engine overheating",
                type = "Routine",
                completed = false,
                upcomingServiceNeeds = "Oil change"
            )

            val vehicleId = vehicleRepository.getVehicleByLicensePlate(vehicle.licensePlate).first().getId()!!

            // Counting the number of maintenances before creating a new one
            val beforeCount = maintenanceRepository.count()

            val mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/vehicles/$vehicleId/maintenances")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andReturn()

            // Counting the number of maintenances after creating a new one
            val afterCount = maintenanceRepository.count()

            assertEquals(afterCount, beforeCount + 1)

            val json = mvcResult.response.contentAsString
            val resultDTO = objectMapper.readValue(json, MaintenanceResDTO::class.java)

            val now = LocalDateTime.now()
            val diffInSeconds = java.time.Duration.between(resultDTO.date, now).seconds
            Assertions.assertTrue(diffInSeconds in -5..5, "Expected maintenance date to be within 5 seconds of now")

            assertEquals(input.defects, resultDTO.defects)
            assertEquals(input.type, resultDTO.type)
            assertEquals(input.completed, resultDTO.completed)
            assertEquals(input.upcomingServiceNeeds, resultDTO.upcomingServiceNeeds)
        }

        @Test
        @Order(4)
        fun `should update a maintenance record`() {
            val input = MaintenanceReqDTO(
                defects = "Leaking pipe",
                type = "Routine",
                completed = false,
                upcomingServiceNeeds = "Check radiator"
            )

            val vehicleId = vehicleRepository.getVehicleByLicensePlate(vehicle.licensePlate).first().getId()!!

            // Counting the number of maintenances before creating a new one
            val beforeCount = maintenanceRepository.count()

            val result = mockMvc.perform(
                MockMvcRequestBuilders.put("/api/v1/vehicles/$vehicleId/maintenances/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andReturn()

            // Counting the number of maintenances after creating a new one
            val afterCount = maintenanceRepository.count()

            assertEquals(afterCount, beforeCount)

            val content = result.response.contentAsString
            val typeRef = object: TypeReference<MaintenanceResDTO>() {}
            val resultDTO = objectMapper.readValue(content, typeRef)

            assertEquals(input.defects, resultDTO.defects)
            assertEquals(input.type, resultDTO.type)
            assertEquals(input.completed, resultDTO.completed)
            assertEquals(input.upcomingServiceNeeds, resultDTO.upcomingServiceNeeds)
        }

        @Test
        @Order(5)
        fun `should delete a maintenance record by id`() {
            // Counting the number of maintenances before deleting one
            val beforeCount = maintenanceRepository.count()

            val vehicleId = vehicleRepository.getVehicleByLicensePlate(vehicle.licensePlate).first().getId()!!

            mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/vehicles/$vehicleId/maintenances/1"))
                .andExpect(status().isNoContent)

            // Counting the number of maintenances after deleting one
            val afterCount = maintenanceRepository.count()

            assertEquals(afterCount, beforeCount - 1)
        }
    }

    @Nested
    inner class CreateMaintenanceErrorTests {
        @Test
        fun `it should return 422 when defects is blank`() {
            val input = MaintenanceReqDTO(
                defects = "",
                type = "Routine",
                completed = false,
                upcomingServiceNeeds = "Check brakes"
            )

            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/vehicles/1/maintenances")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `it should return 422 when type is blank`() {
            val input = MaintenanceReqDTO(
                defects = "Oil leak",
                type = "",
                completed = true,
                upcomingServiceNeeds = "Change oil"
            )

            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/vehicles/1/maintenances")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `it should return 422 when defects and type are both blank`() {
            val input = MaintenanceReqDTO(
                defects = "",
                type = "",
                completed = true,
                upcomingServiceNeeds = "Check tires"
            )

            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/vehicles/1/maintenances")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `it should return 400 when required fields are missing`() {
            val incompleteJson = """
            {
                "completed": false,
                "upcomingServiceNeeds": "Coolant check"
            }
        """.trimIndent()

            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/vehicles/1/maintenances")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(incompleteJson)
            ).andExpect(status().isBadRequest)
        }

        @Test
        fun `it should return 400 when completed is invalid boolean`() {
            val malformedJson = """
            {
                "defects": "Leaking pipe",
                "type": "Emergency",
                "completed": "notaboolean",
                "upcomingServiceNeeds": "Check radiator"
            }
        """.trimIndent()

            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/vehicles/1/maintenances")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson)
            ).andExpect(status().isBadRequest)
        }

        @Test
        fun `it should return 404 when vehicle does not exist`() {
            val input = MaintenanceReqDTO(
                defects = "Faulty AC",
                type = "Routine",
                completed = false,
                upcomingServiceNeeds = "AC filter check"
            )

            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/vehicles/999/maintenances")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().isNotFound)
        }
    }
}

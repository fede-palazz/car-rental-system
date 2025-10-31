package com.rentalcarsystem.reservationservice.integration.controllers

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.rentalcarsystem.reservationservice.integration.BaseIntegrationTest
import com.rentalcarsystem.reservationservice.dtos.request.NoteReqDTO
import com.rentalcarsystem.reservationservice.dtos.response.NoteResDTO
import com.rentalcarsystem.reservationservice.dtos.response.PagedResDTO
import com.rentalcarsystem.reservationservice.enums.*
import com.rentalcarsystem.reservationservice.models.CarModel
import com.rentalcarsystem.reservationservice.models.Note
import com.rentalcarsystem.reservationservice.models.Vehicle
import com.rentalcarsystem.reservationservice.repositories.CarModelRepository
import com.rentalcarsystem.reservationservice.repositories.NoteRepository
import com.rentalcarsystem.reservationservice.repositories.VehicleRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
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
class NoteControllerTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var noteRepository: NoteRepository

    @Autowired
    private lateinit var vehicleRepository: VehicleRepository

    @Autowired
    private lateinit var carModelRepository: CarModelRepository

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    final val carModel = CarModel(
        brand = "Honda",
        model = "Civic",
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
        licensePlate = "ABC1235",
        vin = "2HGBH41JXMN129186",
        carModel = carModel,
        status = CarStatus.AVAILABLE,
        kmTravelled = 15000.0,
        pendingCleaning = false,
        pendingRepair = false
    )

    val note = Note(
        content = "Engine issue noted",
        author = "John",
        date = LocalDateTime.of(2025, 5, 1, 0, 0),
        vehicle = vehicle
    )

    @BeforeAll
    fun setup() {
        // Save car model first
        carModelRepository.save(carModel)

        // Save vehicle next so that it references the existing car model
        vehicleRepository.save(vehicle)

        // Save note next so that it reference the existing vehicle
        vehicle.addNote(note)

        noteRepository.save(note)
    }

    @AfterAll
    fun cleanup() {
        vehicle.removeNote(note)
        vehicleRepository.deleteAll()
        carModelRepository.deleteAll()
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation::class)
    inner class NotesSuccessTests {
        @Test
        @Order(1)
        fun `should return all notes of a vehicle successfully`() {
            val vehicleId = vehicleRepository.getVehicleByLicensePlate(vehicle.licensePlate).first().getId()!!
            val result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/vehicles/$vehicleId/notes"))
                .andExpect(status().isOk).andReturn()

            val content = result.response.contentAsString
            val typeRef = object : TypeReference<PagedResDTO<NoteResDTO>>() {}
            val pagedMaintenances: PagedResDTO<NoteResDTO> = objectMapper.readValue(content, typeRef)

            assertEquals(1, pagedMaintenances.content.size)
            assertEquals(note.content, pagedMaintenances.content.first().content)
            assertEquals(note.author, pagedMaintenances.content.first().author)
            assertEquals(note.date, pagedMaintenances.content.first().date)
        }

        @Test
        @Order(2)
        fun `should create a note for a vehicle`() {
            val note = NoteReqDTO(
                content = "New tire installed",
                author = "Alice",
                date = LocalDateTime.now(ZoneOffset.UTC)
            )

            val vehicleId = vehicleRepository.getVehicleByLicensePlate(vehicle.licensePlate).first().getId()!!

            // Count the number of notes before creating a new one
            val beforeCount = noteRepository.count()

            val result = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/vehicles/$vehicleId/notes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(note))
            ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andReturn()

            // Count the number of notes after creating a new one
            val afterCount = noteRepository.count()

            assertEquals(beforeCount + 1, afterCount)
            val content = result.response.contentAsString
            val typeRef = object : TypeReference<NoteResDTO>() {}
            val noteResDTO: NoteResDTO = objectMapper.readValue(content, typeRef)

            assertEquals(note.content, noteResDTO.content)
            assertEquals(note.author, noteResDTO.author)
            assertEquals(note.date, noteResDTO.date)
        }

        @Test
        @Order(3)
        fun `it should update an existing note for a vehicle`() {
            val updateNote = NoteReqDTO(
                content = "Engine issue resolved",
                author = "John",
                date = LocalDateTime.now(ZoneOffset.UTC)
            )

            val vehicleId = vehicleRepository.getVehicleByLicensePlate(vehicle.licensePlate).first().getId()!!

            // Count the number of notes before updating an existing one
            val beforeCount = noteRepository.count()

            val result = mockMvc.perform(
                MockMvcRequestBuilders.put("/api/v1/vehicles/$vehicleId/notes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateNote))
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andReturn()

            // Count the number of notes after updating an existing one
            val afterCount = noteRepository.count()

            assertEquals(beforeCount, afterCount)

            val content = result.response.contentAsString
            val typeRef = object : TypeReference<NoteResDTO>() {}
            val noteResDTO: NoteResDTO = objectMapper.readValue(content, typeRef)

            assertEquals(updateNote.content, noteResDTO.content)
            assertEquals(updateNote.author, noteResDTO.author)
            assertEquals(updateNote.date, noteResDTO.date)
        }

        @Test
        @Order(4)
        fun `should delete a note by id`() {
            // Count the number of notes before deleting one
            val beforeCount = noteRepository.count()

            val vehicleId = vehicleRepository.getVehicleByLicensePlate(vehicle.licensePlate).first().getId()!!

            mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/vehicles/$vehicleId/notes/1"))
                .andExpect(status().isNoContent)

            // Count the number of notes after deleting one
            val afterCount = noteRepository.count()

            assertEquals(beforeCount - 1, afterCount)
        }
    }

    @Nested
    inner class NoteControllerErrorTests {
        @Test
        fun `GET should return 422 for invalid vehicleId`() {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/vehicles/-1/notes"))
                .andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `GET should return 422 for invalid page`() {
            mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/vehicles/1/notes")
                    .param("page", "-1")
            ).andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `GET should return 422 for invalid size`() {
            mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/vehicles/1/notes")
                    .param("size", "0")
            ).andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `GET should return 422 for invalid sort`() {
            mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/vehicles/1/notes")
                    .param("sort", "badField")
            ).andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `GET should return 422 for invalid sort order`() {
            mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/vehicles/1/notes")
                    .param("order", "fastest")
            ).andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `POST should return 422 for blank content`() {
            val note = NoteReqDTO(content = " ", author = "John", date = LocalDateTime.now(ZoneOffset.UTC))
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/vehicles/1/notes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(note))
            ).andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `POST should return 422 for blank author`() {
            val note = NoteReqDTO(content = "Important note", author = " ", date = LocalDateTime.now(ZoneOffset.UTC))
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/vehicles/1/notes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(note))
            ).andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `POST should return 422 for invalid vehicle id`() {
            val note = NoteReqDTO(content = "Important note", author = "John", date = LocalDateTime.now(ZoneOffset.UTC))
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/vehicles/-1/notes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(note))
            ).andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `PUT should return 422 for invalid note id`() {
            val note = NoteReqDTO(content = "Updated", author = "John", date = LocalDateTime.now(ZoneOffset.UTC))
            mockMvc.perform(
                MockMvcRequestBuilders.put("/api/v1/vehicles/1/notes/0")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(note))
            ).andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `PUT should return 422 for invalid vehicle id`() {
            val note = NoteReqDTO(content = "Updated", author = "John", date = LocalDateTime.now(ZoneOffset.UTC))
            mockMvc.perform(
                MockMvcRequestBuilders.put("/api/v1/vehicles/0/notes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(note))
            ).andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `DELETE should return 422 for invalid vehicle id`() {
            mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/v1/vehicles/0/notes/1")
            ).andExpect(status().isUnprocessableEntity)
        }

        @Test
        fun `DELETE should return 422 for invalid note id`() {
            mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/v1/vehicles/1/notes/0")
            ).andExpect(status().isUnprocessableEntity)
        }
    }
}

package com.rentalcarsystem.reservationservice.integration.endtoend

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rentalcarsystem.reservationservice.dtos.request.NoteReqDTO
import com.rentalcarsystem.reservationservice.enums.*
import com.rentalcarsystem.reservationservice.models.CarModel
import com.rentalcarsystem.reservationservice.models.Vehicle
import com.rentalcarsystem.reservationservice.repositories.CarModelRepository
import com.rentalcarsystem.reservationservice.repositories.NoteRepository
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
class NoteE2ETest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var noteRepository: NoteRepository

    @Autowired
    private lateinit var vehicleRepository: VehicleRepository

    @Autowired
    private lateinit var carModelRepository: CarModelRepository

    private val BASE_URL = "/api/v1/vehicles"
    private var vehicleId: Long? = null
    private var noteId: Long? = null

    @BeforeEach
    fun setup() {
        noteRepository.deleteAll()
        vehicleRepository.deleteAll()
        carModelRepository.deleteAll()
        objectMapper.registerKotlinModule()
    }

    @Test
    fun `should perform complete note lifecycle end-to-end`() {
        // 1. Create prerequisite car model
        val carModel = carModelRepository.save(
            CarModel(
                brand = "Toyota",
                model = "Corolla",
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
                rentalPrice = 70.0
            )
        )

        // 2. Create prerequisite vehicle
        val vehicle = vehicleRepository.save(
            Vehicle(
                licensePlate = "ABC123",
                vin = "1HGCM82633A123456",
                carModel = carModel,
                status = CarStatus.AVAILABLE,
                kmTravelled = 25000.0,
                pendingCleaning = false,
                pendingRepair = false
            )
        )
        vehicleId = vehicle.getId()

        // 3. Create note
        val createRequest = NoteReqDTO(
            content = "Check engine light on",
            author = "John Doe",
            date = LocalDateTime.now()
        )

        val postResult = mockMvc.perform(
            post("$BASE_URL/${vehicleId}/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.content").value("Check engine light on"))
            .andExpect(jsonPath("$.author").value("John Doe"))
            .andReturn()

        val responseJson = postResult.response.contentAsString
        noteId = objectMapper.readTree(responseJson).get("id").asLong()

        // 4. Get notes with filter
        mockMvc.perform(
            get("$BASE_URL/$vehicleId/notes")
                .param("content", "engine")
                .param("author", "John")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "date")
                .param("order", "asc")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].content").value("Check engine light on"))
            .andExpect(jsonPath("$.content[0].author").value("John Doe"))

        // 5. Update note
        val updateRequest = NoteReqDTO(
            content = "Engine light issue resolved",
            author = "John Doe",
            date = LocalDateTime.now()
        )

        mockMvc.perform(
            put("$BASE_URL/$vehicleId/notes/$noteId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").value("Engine light issue resolved"))
            .andExpect(jsonPath("$.author").value("John Doe"))

        // 6. Delete note
        mockMvc.perform(delete("$BASE_URL/$vehicleId/notes/$noteId"))
            .andExpect(status().isNoContent)

        // 7. Verify deletion
        mockMvc.perform(get("$BASE_URL/$vehicleId/notes/$noteId"))
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
        val invalidCreate = NoteReqDTO(
            content = "", // Invalid empty content
            author = "", // Invalid empty author
            date = LocalDateTime.now()
        )

        mockMvc.perform(
            post("$BASE_URL/$vehicleId/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCreate))
        )
            .andExpect(status().isUnprocessableEntity)

        // 3. Test non-existent vehicle ID
        mockMvc.perform(
            post("$BASE_URL/99999/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(NoteReqDTO(
                    content = "Test note",
                    author = "Test author",
                    date = LocalDateTime.now()
                )))
        )
            .andExpect(status().isNotFound)

        // 4. Test invalid pagination parameters
        mockMvc.perform(
            get("$BASE_URL/$vehicleId/notes")
                .param("page", "-1")
                .param("size", "0")
        )
            .andExpect(status().isUnprocessableEntity)

        // 5. Test invalid sort parameters
        mockMvc.perform(
            get("$BASE_URL/$vehicleId/notes")
                .param("sort", "invalidField")
                .param("order", "invalid")
        )
            .andExpect(status().isUnprocessableEntity)
    }
}
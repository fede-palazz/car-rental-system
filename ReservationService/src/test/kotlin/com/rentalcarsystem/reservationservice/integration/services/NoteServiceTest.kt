package com.rentalcarsystem.reservationservice.integration.services

import com.rentalcarsystem.reservationservice.dtos.request.NoteReqDTO
import com.rentalcarsystem.reservationservice.enums.*
import com.rentalcarsystem.reservationservice.filters.NoteFilter
import com.rentalcarsystem.reservationservice.integration.BaseIntegrationTest
import com.rentalcarsystem.reservationservice.models.CarModel
import com.rentalcarsystem.reservationservice.models.Note
import com.rentalcarsystem.reservationservice.models.Vehicle
import com.rentalcarsystem.reservationservice.repositories.CarModelRepository
import com.rentalcarsystem.reservationservice.repositories.NoteRepository
import com.rentalcarsystem.reservationservice.repositories.VehicleRepository
import com.rentalcarsystem.reservationservice.services.NoteService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class NoteServiceTest : BaseIntegrationTest() {


    @Autowired
    private lateinit var noteService: NoteService

    @Autowired
    lateinit var carModelRepository: CarModelRepository

    @Autowired
    lateinit var vehicleRepository: VehicleRepository

    @Autowired
    lateinit var noteRepository: NoteRepository

    private lateinit var carModel: CarModel
    private lateinit var vehicle: Vehicle
    private lateinit var note: Note

    @BeforeEach
    fun setup() {
        carModel = carModelRepository.save(
            CarModel(
                brand = "Toyota",
                model = "Yaris",
                year = "2022",
                segment = CarSegment.COMPACT,
                doorsNumber = 4,
                seatingCapacity = 5,
                luggageCapacity = 100.0,
                category = CarCategory.ECONOMY,
                engineType = EngineType.ELECTRIC,
                transmissionType = TransmissionType.AUTOMATIC,
                drivetrain = Drivetrain.FWD,
                motorDisplacement = 4,
                rentalPrice = 50.0
            )
        )

        vehicle = vehicleRepository.save(
            Vehicle(
                licensePlate = "XYZ1234",
                vin = "1HGBH41JXMN109186",
                carModel = carModel,
                status = CarStatus.AVAILABLE,
                kmTravelled = 15000.0,
                pendingCleaning = false,
                pendingRepair = false
            )
        )

        note = noteRepository.save(
            Note(
                vehicle = vehicle,
                content = "Check engine light on",
                author = "Admin",
                date = LocalDateTime.of(2025, 5, 10, 0, 0)
            )
        )
    }

    @AfterEach
    fun cleanup() {
        noteRepository.deleteAll()
        vehicleRepository.deleteAll()
        carModelRepository.deleteAll()
    }

    @Test
    fun `should get notes by content filter`() {
        val filters = NoteFilter(content = "engine")

        val result = noteService.getNotes(
            page = 0,
            size = 10,
            sortBy = "date",
            sortOrder = "asc",
            vehicleId = vehicle.getId()!!,
            filters = filters
        )

        assertThat(result.content).hasSize(1)
        assertThat(result.content.first().content).contains("engine")
    }

    @Test
    fun `should filter notes by author and date`() {
        val filters = NoteFilter(
            author = "Admin",
            minDate = LocalDate.of(2025, 5, 10),
            maxDate = LocalDate.of(2025, 5, 20)
        )

        val result = noteService.getNotes(
            page = 0,
            size = 10,
            sortBy = "date",
            sortOrder = "asc",
            vehicleId = vehicle.getId()!!,
            filters = filters
        )

        assertThat(result.content).hasSize(1)
        assertThat(result.content.first().author).isEqualTo("Admin")
    }

    @Test
    fun `should return empty if no notes match filters`() {
        val filters = NoteFilter(content = "nonexistent")

        val result = noteService.getNotes(
            page = 0,
            size = 5,
            sortBy = "date",
            sortOrder = "asc",
            vehicleId = vehicle.getId()!!,
            filters = filters
        )

        assertThat(result.content).isEmpty()
    }

    @Test
    fun `should create a new note`() {
        val req = NoteReqDTO(
            content = "New maintenance note",
            author = "Tech",
            date = LocalDateTime.of(2025, 5, 16, 0, 0)
        )

        val res = noteService.createNote(vehicle.getId()!!, req)

        assertThat(res.content).isEqualTo("New maintenance note")
        assertThat(res.author).isEqualTo("Tech")
    }

    @Test
    fun `should update a note`() {
        val req = NoteReqDTO(
            content = "Updated note",
            author = "UpdatedAuthor",
            date = LocalDateTime.of(2025, 5, 18, 0, 0)
        )

        val res = noteService.updateNote(note.getId()!!, vehicle.getId()!!, req)

        assertThat(res.content).isEqualTo("Updated note")
        assertThat(res.author).isEqualTo("UpdatedAuthor")
    }

    @Test
    fun `should delete a note`() {
        val toDelete = noteRepository.save(
            Note(
                vehicle = vehicle,
                content = "Temporary note",
                author = "User",
                date = LocalDateTime.of(2025, 5, 16, 0, 0)
            )
        )

        noteService.deleteNote(vehicle.getId()!!, toDelete.getId()!!)

        val deleted = noteRepository.findById(toDelete.getId()!!)
        assertThat(deleted).isEmpty()
    }

    @Test
    fun `should throw exception when updating note with wrong vehicleId`() {
        val req = NoteReqDTO(
            content = "Invalid update",
            author = "Hacker",
            date = LocalDateTime.of(2025, 5, 18, 0, 0)
        )
        val otherVehicle = vehicleRepository.save(
            Vehicle(
                licensePlate = "ABC1234",
                vin = "2HGBH41JXMN109999",
                carModel = carModel,
                status = CarStatus.AVAILABLE,
                kmTravelled = 2000.0,
                pendingCleaning = false,
                pendingRepair = false
            )
        )

        assertThatThrownBy {
            noteService.updateNote(note.getId()!!, otherVehicle.getId()!!, req)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("does not belong to vehicle")
    }

    @Test
    fun `should throw exception when deleting note with wrong vehicleId`() {
        val otherVehicle = vehicleRepository.save(
            Vehicle(
                licensePlate = "DEF5678",
                vin = "3HGBH41JXMN109888",
                carModel = carModel,
                status = CarStatus.AVAILABLE,
                kmTravelled = 5000.0,
                pendingCleaning = false,
                pendingRepair = false
            )
        )

        assertThatThrownBy {
            noteService.deleteNote(otherVehicle.getId()!!, note.getId()!!)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("does not belong to vehicle")
    }

    @Test
    fun `should accept note with null date and set default`() {
        val req = NoteReqDTO(
            content = "Note without date",
            author = "Author",
            date = null
        )
        val res = noteService.createNote(vehicle.getId()!!, req)
        assertThat(res.date).isNotNull
    }

    @Test
    fun `should throw when creating note for non-existent vehicle`() {
        val req = NoteReqDTO(
            content = "Note content",
            author = "Author",
            date = LocalDateTime.now(ZoneOffset.UTC)
        )
        val invalidVehicleId = 9999L
        Assertions.assertThrows(RuntimeException::class.java) {
            noteService.createNote(invalidVehicleId, req)
        }
    }

    @Test
    fun `should throw when updating note with mismatched vehicle`() {
        val anotherVehicle = vehicleRepository.save(
            Vehicle(
                licensePlate = "ABC9999",
                vin = "2HGBH41JXMN109186",
                carModel = carModel,
                status = CarStatus.AVAILABLE,
                kmTravelled = 10000.0,
                pendingCleaning = false,
                pendingRepair = false
            )
        )
        val req = NoteReqDTO(
            content = "Updated content",
            author = "Author",
            date = LocalDateTime.now(ZoneOffset.UTC)
        )
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            noteService.updateNote(note.getId()!!, anotherVehicle.getId()!!, req)
        }
    }

    @Test
    fun `should throw when deleting note with mismatched vehicle`() {
        val anotherVehicle = vehicleRepository.save(
            Vehicle(
                licensePlate = "DEF7777",
                vin = "3HGBH41JXMN109186",
                carModel = carModel,
                status = CarStatus.AVAILABLE,
                kmTravelled = 12000.0,
                pendingCleaning = false,
                pendingRepair = false
            )
        )
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            noteService.deleteNote(anotherVehicle.getId()!!, note.getId()!!)
        }
    }

    @Test
    fun `should return all notes if filters are empty`() {
        // Add one more note
        val extraNote = noteRepository.save(
            Note(
                vehicle = vehicle,
                content = "Extra note",
                author = "AnotherAuthor",
                date = LocalDateTime.now(ZoneOffset.UTC)
            )
        )
        val filters = NoteFilter()

        val result = noteService.getNotes(0, 10, "date", "asc", vehicle.getId()!!, filters)

        assertThat(result.content).hasSizeGreaterThanOrEqualTo(2)
        assertThat(result.content.map { it.content }).containsExactlyInAnyOrder(note.content, extraNote.content)
    }

}

package com.rentalcarsystem.reservationservice.integration.services

import com.rentalcarsystem.reservationservice.dtos.request.MaintenanceReqDTO
import com.rentalcarsystem.reservationservice.enums.CarCategory
import com.rentalcarsystem.reservationservice.enums.CarSegment
import com.rentalcarsystem.reservationservice.enums.CarStatus
import com.rentalcarsystem.reservationservice.enums.Drivetrain
import com.rentalcarsystem.reservationservice.enums.EngineType
import com.rentalcarsystem.reservationservice.enums.TransmissionType
import com.rentalcarsystem.reservationservice.exceptions.FailureException
import com.rentalcarsystem.reservationservice.filters.MaintenanceFilter
import com.rentalcarsystem.reservationservice.integration.BaseIntegrationTest
import com.rentalcarsystem.reservationservice.models.CarModel
import com.rentalcarsystem.reservationservice.models.Maintenance
import com.rentalcarsystem.reservationservice.models.Vehicle
import com.rentalcarsystem.reservationservice.repositories.CarModelRepository
import com.rentalcarsystem.reservationservice.repositories.MaintenanceRepository
import com.rentalcarsystem.reservationservice.repositories.VehicleRepository
import com.rentalcarsystem.reservationservice.services.MaintenanceService
import org.assertj.core.api.Assertions.assertThat
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
class MaintenanceServiceTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var maintenanceService: MaintenanceService

    @Autowired
    private lateinit var maintenanceRepository: MaintenanceRepository

    @Autowired
    lateinit var carModelRepository: CarModelRepository

    @Autowired
    lateinit var vehicleRepository: VehicleRepository

    private lateinit var carModel: CarModel
    private lateinit var vehicle: Vehicle
    private lateinit var maintenance: Maintenance
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

        maintenance = maintenanceRepository.save(
            Maintenance(
                vehicle = vehicle,
                defects = "Oil change",
                type = "Routine",
                completed = false,
                upcomingServiceNeeds = "Tire rotation",
                date = LocalDateTime.of(2025, 5, 1, 0, 0)
            )
        )
    }

    @AfterEach
    fun cleanup() {
        maintenanceRepository.deleteAll()
        vehicleRepository.deleteAll()
        carModelRepository.deleteAll()
    }


    @Test
    fun `should get maintenance by id`() {
        val result = maintenanceService.getMaintenanceById(vehicle.getId()!!, maintenance.getId()!!)
        assertThat(result.defects).isEqualTo("Oil change")
        assertThat(result.type).isEqualTo("Routine")
    }


    @Test
    fun `should get maintenances by defects filter`() {
        val filters = MaintenanceFilter(defects = "Oil change")

        val result = maintenanceService.getMaintenances(
            vehicleId = vehicle.getId()!!,
            page = 0,
            size = 10,
            sortBy = "date",
            sortOrder = "asc",
            filters = filters
        )

        assertThat(result.content).hasSize(1)
        assertThat(result.content.first().defects).contains("Oil change")
    }

    @Test
    fun `should filter by type and upcoming service needs`() {
        val filters = MaintenanceFilter(
            defects = "Oil change",
            upcomingServiceNeeds = "Tire rotation"
        )

        val result = maintenanceService.getMaintenances(
            vehicleId = vehicle.getId()!!,
            page = 0,
            size = 10,
            sortBy = "date",
            sortOrder = "asc",
            filters = filters
        )

        assertThat(result.content).hasSize(1)
        assertThat(result.content.first().defects).contains("Oil change")
        assertThat(result.content.first().upcomingServiceNeeds).contains("Tire rotation")
    }

    @Test
    fun `should filter by date range`() {
        val filters = MaintenanceFilter(
            minDate = LocalDate.of(2025, 4, 30),
            maxDate = LocalDate.of(2025, 5, 2)
        )

        val result = maintenanceService.getMaintenances(
            vehicleId = vehicle.getId()!!,
            page = 0,
            size = 5,
            sortBy = "date",
            sortOrder = "asc",
            filters = filters
        )

        assertThat(result.content).hasSize(1)
    }


    @Test
    fun `should return empty for non-matching filter`() {
        val filters = MaintenanceFilter(defects = "Nonexistent defect")

        val result = maintenanceService.getMaintenances(
            vehicleId = vehicle.getId()!!,
            page = 0,
            size = 5,
            sortBy = "date",
            sortOrder = "asc",
            filters = filters
        )

        assertThat(result.content).isEmpty()
    }

    @Test
    fun `should create a new maintenance and update vehicle status`() {
        val req = MaintenanceReqDTO(
            defects = "Oil leak",
            completed = false,
            type = "Engine",
            upcomingServiceNeeds = "Check gasket"
        )

        val res = maintenanceService.createMaintenance(vehicle.getId()!!, req)

        assertThat(res.defects).isEqualTo("Oil leak")
        val updatedVehicle = vehicleRepository.findById(vehicle.getId()!!).get()
        assertThat(updatedVehicle.status).isEqualTo(CarStatus.IN_MAINTENANCE)
    }

    @Test
    fun `should update a maintenance and set vehicle status to available when completed`() {
        val req = MaintenanceReqDTO(
            defects = "Fixed all issues",
            completed = true,
            type = "General",
            upcomingServiceNeeds = null
        )

        val res = maintenanceService.updateMaintenance(vehicle.getId()!!, maintenance.getId()!!, req)

        assertThat(res.completed).isTrue()
        val updatedVehicle = vehicleRepository.findById(vehicle.getId()!!).get()
        assertThat(updatedVehicle.status).isEqualTo(CarStatus.AVAILABLE)
    }

    @Test
    fun `should delete a maintenance`() {
        val toDelete = maintenanceRepository.save(
            Maintenance(
                vehicle = vehicle,
                defects = "Oil change",
                type = "Routine",
                completed = false,
                upcomingServiceNeeds = "Tire rotation",
                date = LocalDateTime.of(2025, 5, 1, 0, 0)
            )
        )

        maintenanceService.deleteMaintenance(vehicle.getId()!!, toDelete.getId()!!)

        val deleted = maintenanceRepository.findById(toDelete.getId()!!)
        assertThat(deleted).isEmpty()
    }

    @Test
    fun `should not allow creation of completed maintenance`() {
        val req = MaintenanceReqDTO(
            defects = "Complete already",
            completed = true,
            type = "Routine",
            upcomingServiceNeeds = null
        )

        val exception = assertThrows<IllegalArgumentException> {
            maintenanceService.createMaintenance(vehicle.getId()!!, req)
        }

        assertThat(exception.message).isEqualTo("A newly created maintenance record cannot be completed")
    }

    @Test
    fun `should not update a completed maintenance`() {
        maintenance.completed = true
        maintenanceRepository.save(maintenance)

        val req = MaintenanceReqDTO(
            defects = "Attempted update",
            completed = true,
            type = "Routine",
            upcomingServiceNeeds = "Check oil"
        )

        val exception = assertThrows<IllegalArgumentException> {
            maintenanceService.updateMaintenance(vehicle.getId()!!, maintenance.getId()!!, req)
        }

        assertThat(exception.message).contains("already completed")
    }

    @Test
    fun `should throw when maintenance ID does not exist`() {
        val exception = assertThrows<FailureException> {
            maintenanceService.getMaintenanceById(vehicle.getId()!!, 99999L)
        }
        assertThat(exception.message).contains("not found")
    }

    @Test
    fun `should throw when maintenance does not belong to vehicle`() {
        val anotherVehicle = vehicleRepository.save(
            Vehicle(
                licensePlate = "ZZZ999",
                vin = "1HGBH41JXMN109187",
                carModel = carModel,
                status = CarStatus.AVAILABLE,
                kmTravelled = 1000.0,
                pendingCleaning = false,
                pendingRepair = false
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            maintenanceService.getMaintenanceById(anotherVehicle.getId()!!, maintenance.getId()!!)
        }

        assertThat(exception.message).contains("does not belong to vehicle")
    }

    @Test
    fun `should not delete maintenance if it does not belong to vehicle`() {
        val anotherVehicle = vehicleRepository.save(
            Vehicle(
                licensePlate = "AAA111",
                vin = "2HGBH41JXMN109188",
                carModel = carModel,
                status = CarStatus.AVAILABLE,
                kmTravelled = 10000.0,
                pendingCleaning = false,
                pendingRepair = false
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            maintenanceService.deleteMaintenance(anotherVehicle.getId()!!, maintenance.getId()!!)
        }

        assertThat(exception.message).contains("does not belong to vehicle")
    }

    @Test
    fun `should not update maintenance if it does not belong to vehicle`() {
        val anotherVehicle = vehicleRepository.save(
            Vehicle(
                licensePlate = "BBB222",
                vin = "3HGBH41JXMN109189",
                carModel = carModel,
                status = CarStatus.AVAILABLE,
                kmTravelled = 10000.0,
                pendingCleaning = false,
                pendingRepair = false
            )
        )

        val req = MaintenanceReqDTO(
            defects = "Some update",
            completed = false,
            type = "General",
            upcomingServiceNeeds = "Brake check"
        )

        val exception = assertThrows<IllegalArgumentException> {
            maintenanceService.updateMaintenance(anotherVehicle.getId()!!, maintenance.getId()!!, req)
        }

        assertThat(exception.message).contains("does not belong to vehicle")
    }


}

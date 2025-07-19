package com.rentalcarsystem.reservationservice.integration.services

import com.rentalcarsystem.reservationservice.dtos.request.VehicleReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.VehicleUpdateReqDTO
import com.rentalcarsystem.reservationservice.enums.CarCategory
import com.rentalcarsystem.reservationservice.enums.CarSegment
import com.rentalcarsystem.reservationservice.enums.CarStatus
import com.rentalcarsystem.reservationservice.enums.Drivetrain
import com.rentalcarsystem.reservationservice.enums.EngineType
import com.rentalcarsystem.reservationservice.enums.TransmissionType
import com.rentalcarsystem.reservationservice.exceptions.FailureException
import com.rentalcarsystem.reservationservice.filters.VehicleFilter
import com.rentalcarsystem.reservationservice.integration.BaseIntegrationTest
import com.rentalcarsystem.reservationservice.models.CarModel
import com.rentalcarsystem.reservationservice.models.Vehicle
import com.rentalcarsystem.reservationservice.repositories.CarModelRepository
import com.rentalcarsystem.reservationservice.repositories.VehicleRepository
import com.rentalcarsystem.reservationservice.services.VehicleService
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class VehicleServiceImplIntegrationTest: BaseIntegrationTest() {

    @Autowired
    private lateinit var vehicleService: VehicleService

    @Autowired
    lateinit var carModelRepository: CarModelRepository

    @Autowired
    lateinit var vehicleRepository: VehicleRepository

    private lateinit var carModel: CarModel
    private lateinit var vehicle: Vehicle
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
    }

    @AfterEach
    fun cleanup() {
        vehicleRepository.deleteAll()
        carModelRepository.deleteAll()
    }

    @Test
    fun `should add a new vehicle successfully`() {
        val dto = VehicleReqDTO(
            licensePlate = "TEST123",
            vin = "1HGCM82633A004999",
            status = CarStatus.AVAILABLE,
            kmTravelled = 12000.0,
            pendingCleaning = false,
            pendingRepair = false,
            carModelId = carModel.getId()!!
        )

        val result = vehicleService.addVehicle(dto)

        assertEquals("TEST123", result.licensePlate)
        assertEquals("1HGCM82633A004999", result.vin)
        assertEquals("Toyota", result.brand)
        assertEquals("Yaris", result.model)
    }

    @Test
    fun `should throw on duplicate license plate`() {
        val dto = VehicleReqDTO(
            licensePlate = vehicle.licensePlate,
            vin = "DIFFVIN1234567891",
            status = CarStatus.AVAILABLE,
            kmTravelled = 1000.0,
            pendingCleaning = false,
            pendingRepair = false,
            carModelId = carModel.getId()!!
        )

        val ex = assertThrows<FailureException> {
            vehicleService.addVehicle(dto)
        }

        assertTrue(ex.message!!.contains("already exists"))
    }

    @Test
    fun `should get vehicle by ID`() {
        val found = vehicleService.getVehicleById(vehicle.getId()!!)
        assertEquals(vehicle.getId(), found.getId())
        assertEquals(vehicle.licensePlate, found.licensePlate)
    }

    @Test
    fun `should update a vehicle`() {
        val update = VehicleUpdateReqDTO(
            licensePlate = "UPDATED",
            status = CarStatus.RENTED,
            kmTravelled = 16000.0,
            pendingCleaning = true,
            pendingRepair = true
        )

        val updated = vehicleService.updateVehicle(vehicle.getId()!!, update)

        assertEquals("UPDATED", updated.licensePlate)
        assertEquals(CarStatus.RENTED, updated.status)
        assertEquals(16000.0, updated.kmTravelled)
        assertTrue(updated.pendingCleaning)
        assertTrue(updated.pendingRepair)
    }

    @Test
    fun `should delete a vehicle`() {
        val toDelete = vehicleService.addVehicle(
            VehicleReqDTO(
                licensePlate = "DEL1234",
                vin = "DELVIN12345678910",
                status = CarStatus.AVAILABLE,
                kmTravelled = 0.0,
                pendingCleaning = false,
                pendingRepair = false,
                carModelId = carModel.getId()!!
            )
        )

        vehicleService.deleteVehicle(toDelete.id)

        val ex = assertThrows<FailureException> {
            vehicleService.getVehicleById(toDelete.id)
        }

        assertTrue(ex.message!!.contains("not found"))
    }

    @Test
    fun `should filter vehicles by brand`() {
        val filter = VehicleFilter(brand = "Toyota", licensePlate = null)

        val result = vehicleService.getVehicles(
            page = 0,
            size = 10,
            sortBy = "id",
            sortOrder = "asc",
            filters = filter
        )

        assertTrue(result.content.any { it.brand == "Toyota" })
    }
    @Test
    fun `should throw when adding vehicle with non-existent car model`() {
        val dto = VehicleReqDTO(
            licensePlate = "NEW123",
            vin = "VINNONEXISTENT12345",
            status = CarStatus.AVAILABLE,
            kmTravelled = 0.0,
            pendingCleaning = false,
            pendingRepair = false,
            carModelId = 999999L
        )

        assertThrows<ConstraintViolationException> {
            vehicleService.addVehicle(dto)
        }
    }

    @Test
    fun `should update vehicle partially when some fields are null`() {
        val update = VehicleUpdateReqDTO(
            licensePlate = null,
            status = CarStatus.RENTED,
            kmTravelled = 17000.0,
            pendingCleaning = null,
            pendingRepair = null
        )

        val updated = vehicleService.updateVehicle(vehicle.getId()!!, update)

        assertEquals(vehicle.licensePlate, updated.licensePlate)
        assertEquals(CarStatus.RENTED, updated.status)
        assertEquals(17000.0, updated.kmTravelled)
        assertFalse(updated.pendingCleaning)
        assertFalse(updated.pendingRepair)
    }


    @Test
    fun `should filter vehicles by license plate and year`() {
        val filter = VehicleFilter(
            licensePlate = "XYZ",
            year = 2022
        )

        val result = vehicleService.getVehicles(
            page = 0,
            size = 10,
            sortBy = "licensePlate",
            sortOrder = "asc",
            filters = filter
        )

        assertTrue(result.content.all { it.licensePlate.startsWith("XYZ") && it.year == "2022" })
    }

    @Test
    fun `get vehicle by non-existent id should throw`() {
        val ex = assertThrows<FailureException> {
            vehicleService.getVehicleById(999999L)
        }
        assertTrue(ex.message!!.contains("not found"))
    }

}

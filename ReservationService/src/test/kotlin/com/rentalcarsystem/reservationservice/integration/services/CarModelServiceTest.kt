package com.rentalcarsystem.reservationservice.integration.services

import com.rentalcarsystem.reservationservice.dtos.request.CarModelReqDTO
import com.rentalcarsystem.reservationservice.enums.*
import com.rentalcarsystem.reservationservice.exceptions.FailureException
import com.rentalcarsystem.reservationservice.filters.CarModelFilter
import com.rentalcarsystem.reservationservice.integration.BaseIntegrationTest
import com.rentalcarsystem.reservationservice.models.CarFeature
import com.rentalcarsystem.reservationservice.models.CarModel
import com.rentalcarsystem.reservationservice.repositories.CarFeatureRepository
import com.rentalcarsystem.reservationservice.repositories.CarModelRepository
import com.rentalcarsystem.reservationservice.services.CarModelService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class CarModelServiceTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var carModelService: CarModelService

    @Autowired
    private lateinit var carModelRepository: CarModelRepository

    @Autowired
    private lateinit var carFeatureRepository: CarFeatureRepository

    private lateinit var model: CarModel
    private lateinit var gps: CarFeature
    private lateinit var aircon: CarFeature

    @BeforeEach
    fun setup() {

         gps = CarFeature(description = "GPS")
         aircon = CarFeature(description = "Air Conditioning")

         model = CarModel(
            brand = "BMW",
            model = "320i",
            year = "2021",
            segment = CarSegment.COMPACT,
            doorsNumber = 4,
            seatingCapacity = 5,
            luggageCapacity = 3.0,
            category = CarCategory.PREMIUM,
            engineType = EngineType.ELECTRIC,
            transmissionType = TransmissionType.AUTOMATIC,
            drivetrain = Drivetrain.RWD,
            motorDisplacement = 2,
            rentalPrice = 70.0,
            features = mutableSetOf(gps, aircon)
        )
        carFeatureRepository.save(gps)
        carFeatureRepository.save(aircon)
        carModelRepository.save(model)

       }
    @AfterEach
    fun cleanup() {
        carModelRepository.deleteAll()
        carFeatureRepository.deleteAll()
    }

    @Test
    fun `should find car models by brand`() {
        val filters = CarModelFilter(brand = "BMW")

        val response = carModelService.getAllModels(
            page = 0,
            size = 5,
            sortBy = "brand",
            sortOrder = "asc",
            filters = filters,
            desiredPickUpDate = null,
            desiredDropOffDate = null,
            singlePage = true
        )

        assertThat(response.content).hasSize(1)
        val result = response.content.first()
        assertThat(result.brand).isEqualTo("BMW")
        assertThat(result.model).isEqualTo("320i")
        assertThat(result.category).isEqualTo(CarCategory.PREMIUM)
        assertThat(result.segment).isEqualTo(CarSegment.COMPACT)
    }

    @Test
    fun `should filter by category and engine type`() {
        val filters = CarModelFilter(
            category = CarCategory.PREMIUM,
            engineType = EngineType.ELECTRIC
        )

        val response = carModelService.getAllModels(
            page = 0,
            size = 5,
            sortBy = "rentalPrice",
            sortOrder = "asc",
            filters = filters,
            desiredPickUpDate = null,
            desiredDropOffDate = null,
            singlePage = true
        )

        assertThat(response.content).hasSize(1)
        assertThat(response.content.first().engineType).isEqualTo(EngineType.ELECTRIC)
    }

    @Test
    fun `should return empty list for non-matching filters`() {
        val filters = CarModelFilter(
            brand = "Tesla",
            engineType = EngineType.HYBRID
        )

        val response = carModelService.getAllModels(
            page = 0,
            size = 5,
            sortBy = "brand",
            sortOrder = "asc",
            filters = filters,
            desiredPickUpDate = null,
            desiredDropOffDate = null,
            singlePage = true
        )

        assertThat(response.content).isEmpty()
    }
    @Test
    fun `should fetch car model by id`() {
        val result = carModelService.getCarModelById(model.getId()!!)
        assertThat(result.id).isEqualTo(model.getId())
        assertThat(result.model).isEqualTo("320i")
    }

    @Test
    fun `should fetch all car features`() {
        val result = carModelService.getAllCarFeatures()
        assertThat(result).hasSize(2)
    }

    @Test
    fun `should fetch car feature by id`() {
        val result = carModelService.getCarFeatureById(gps.getId()!!)
        assertThat(result.id).isEqualTo(gps.getId())
        assertThat(result.description).isEqualTo("GPS")
    }

    @Test
    fun `should fetch features by car model id`() {
        val result = carModelService.getCarFeaturesByCarModelId(model.getId()!!)
        assertThat(result.map { it.description }).containsExactlyInAnyOrder("GPS", "Air Conditioning")
    }

    @Test
    fun `should create a new car model`() {
        val dto = CarModelReqDTO(
            brand = "Audi",
            model = "A4",
            year = "2023",
            segment = CarSegment.COMPACT,
            doorsNumber = 4,
            seatingCapacity = 5,
            luggageCapacity = 4.0,
            category = CarCategory.MIDSIZE,
            engineType = EngineType.HYBRID,
            transmissionType = TransmissionType.MANUAL,
            drivetrain = Drivetrain.FWD,
            motorDisplacement = 1,
            rentalPrice = 60.0,
            featureIds = listOf(gps.getId()!!)
        )
        val result = carModelService.createCarModel(dto)
        assertThat(result.brand).isEqualTo("Audi")
        assertThat(result.features.map { it.id }).contains(gps.getId()!!)
    }

    @Test
    fun `should update a car model`() {
        val dto = CarModelReqDTO(
            brand = "BMW",
            model = "330i",
            year = "2022",
            segment = CarSegment.COMPACT,
            doorsNumber = 4,
            seatingCapacity = 5,
            luggageCapacity = 3.5,
            category = CarCategory.PREMIUM,
            engineType = EngineType.ELECTRIC,
            transmissionType = TransmissionType.AUTOMATIC,
            drivetrain = Drivetrain.RWD,
            motorDisplacement = 2,
            rentalPrice = 75.0,
            featureIds = listOf(gps.getId()!!)
        )
        val result = carModelService.updateCarModel(model.getId()!!, dto)
        assertThat(result.model).isEqualTo("330i")
        assertThat(result.luggageCapacity).isEqualTo(3.5)
    }

    @Test
    fun `should delete a car model`() {
        val toDelete = carModelRepository.save(
            CarModel(
                brand = "Ford",
                model = "Focus",
                year = "2020",
                segment = CarSegment.COMPACT,
                doorsNumber = 4,
                seatingCapacity = 5,
                luggageCapacity = 3.0,
                category = CarCategory.ECONOMY,
                engineType = EngineType.PETROL,
                transmissionType = TransmissionType.MANUAL,
                drivetrain = Drivetrain.FWD,
                motorDisplacement = 1,
                rentalPrice = 50.0,
                features = mutableSetOf(gps)
            )
        )
        carModelService.deleteCarModelById(toDelete.getId()!!)
        val exists = carModelRepository.findById(toDelete.getId()!!)
        assertThat(exists).isEmpty
    }
    @Test
    fun `createCarModel throws exception when feature IDs are invalid`() {
        val invalidFeatureIds = listOf(999L, 888L)
        val dto = CarModelReqDTO(
            brand = "Honda",
            model = "Civic",
            year = "2023",
            segment = CarSegment.COMPACT,
            doorsNumber = 4,
            seatingCapacity = 5,
            luggageCapacity = 400.0,
            category = CarCategory.ECONOMY,
            engineType = EngineType.PETROL,
            transmissionType = TransmissionType.AUTOMATIC,
            drivetrain = Drivetrain.FWD,
            motorDisplacement = 1,
            rentalPrice = 55.0,
            featureIds = invalidFeatureIds
        )

        assertThrows<FailureException> {
            carModelService.createCarModel(dto)
        }
    }
    @Test
    fun `updateCarModel throws exception when CarModel ID does not exist`() {
        val dto = CarModelReqDTO(
            brand = "Honda",
            model = "Civic",
            year = "2023",
            segment = CarSegment.COMPACT,
            doorsNumber = 4,
            seatingCapacity = 5,
            luggageCapacity = 400.0,
            category = CarCategory.ECONOMY,
            engineType = EngineType.PETROL,
            transmissionType = TransmissionType.AUTOMATIC,
            drivetrain = Drivetrain.FWD,
            motorDisplacement = 1,
            rentalPrice = 55.0,
            featureIds = listOf(gps.getId()!!)
        )

        val exception = assertThrows<FailureException> {
            carModelService.updateCarModel(id = 999L, model = dto)
        }

        Assertions.assertTrue(exception.message!!.contains("Car model with ID 999 not found"))
    }

    @Test
    fun `getCarModelById throws exception for invalid ID`() {
        val exception = assertThrows<FailureException> {
            carModelService.getCarModelById(999L)
        }

        Assertions.assertTrue(exception.message!!.contains("Car model with ID 999 not found"))
    }

    @Test
    fun `deleteCarModelById throws exception when CarModel ID is invalid`() {
        val exception = assertThrows<FailureException> {
            carModelService.deleteCarModelById(999L)
        }

        Assertions.assertTrue(exception.message!!.contains("Car model with ID 999 not found"))
    }

    @Test
    fun `getCarFeaturesByCarModelId throws exception for invalid CarModel ID`() {
        val exception = assertThrows<FailureException> {
            carModelService.getCarFeaturesByCarModelId(999L)
        }

        Assertions.assertTrue(exception.message!!.contains("Car model with ID 999 not found"))
    }
}

package com.rentalcarsystem.reservationservice.services

import com.rentalcarsystem.reservationservice.dtos.request.VehicleReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.VehicleUpdateReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.toEntity
import com.rentalcarsystem.reservationservice.dtos.response.PagedResDTO
import com.rentalcarsystem.reservationservice.dtos.response.VehicleResDTO
import com.rentalcarsystem.reservationservice.dtos.response.toResDTO
import com.rentalcarsystem.reservationservice.exceptions.FailureException
import com.rentalcarsystem.reservationservice.exceptions.ResponseEnum
import com.rentalcarsystem.reservationservice.filters.VehicleFilter
import com.rentalcarsystem.reservationservice.models.CarModel
import com.rentalcarsystem.reservationservice.models.Vehicle
import com.rentalcarsystem.reservationservice.repositories.CarModelRepository
import com.rentalcarsystem.reservationservice.repositories.ReservationRepository
import com.rentalcarsystem.reservationservice.repositories.VehicleRepository
import jakarta.persistence.criteria.Join
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import java.time.LocalDateTime

@Service
@Validated
@Transactional
class VehicleServiceImpl(
    private val carModelRepository: CarModelRepository,
    private val reservationRepository: ReservationRepository,
    private val vehicleRepository: VehicleRepository,
    @Value("\${reservation.buffer-days}")
    private val reservationBufferDays: Long
) : VehicleService {
    override fun getVehicles(
        page: Int,
        size: Int,
        sortBy: String,
        sortOrder: String,
        @Valid filters: VehicleFilter,
    ): PagedResDTO<VehicleResDTO> {
        var spec: Specification<Vehicle> = Specification.where(null)
        // License plate
        filters.licensePlate?.takeIf { it.isNotBlank() }?.let { licensePlate ->
            spec = spec.and { root, _, cb ->
                cb.like(cb.lower(root.get("licensePlate")), "${licensePlate.lowercase()}%")
            }
        }
        // VIN
        filters.vin?.takeIf { it.isNotBlank() }?.let { vin ->
            spec = spec.and { root, _, cb ->
                cb.like(cb.lower(root.get("vin")), "${vin.lowercase()}%")
            }
        }
        // Brand
        filters.brand?.takeIf { it.isNotBlank() }?.let { brand ->
            spec = spec.and { root, _, cb ->
                val carModelJoin: Join<Vehicle, CarModel> = root.join("carModel")
                cb.like(cb.lower(carModelJoin.get("brand")), "${brand.lowercase()}%")
            }
        }
        // Model
        filters.model?.takeIf { it.isNotBlank() }?.let { model ->
            spec = spec.and { root, _, cb ->
                val carModelJoin: Join<Vehicle, CarModel> = root.join("carModel")
                cb.like(cb.lower(carModelJoin.get("model")), "${model.lowercase()}%")
            }
        }
        // Year
        filters.year?.let { year ->
            spec = spec.and { root, _, cb ->
                val carModelJoin: Join<Vehicle, CarModel> = root.join("carModel")
                cb.equal(carModelJoin.get<Int>("year"), year)
            }
        }
        // Km travelled
        filters.minKmTravelled?.let { minKm ->
            spec = spec.and { root, _, cb ->
                cb.greaterThanOrEqualTo(root.get("kmTravelled"), minKm)
            }
        }
        filters.maxKmTravelled?.let { maxKm ->
            spec = spec.and { root, _, cb ->
                cb.lessThanOrEqualTo(root.get("kmTravelled"), maxKm)
            }
        }
        // Pending cleaning
        filters.pendingCleaning?.let { cleaning ->
            spec = spec.and { root, _, cb ->
                cb.equal(root.get<Boolean>("pendingCleaning"), cleaning)
            }
        }
        // Pending repair
        filters.pendingRepair?.let { repair ->
            spec = spec.and { root, _, cb ->
                cb.equal(root.get<Boolean>("pendingRepair"), repair)
            }
        }
        // Sorting
        val sortOrd: Sort.Direction = if (sortOrder == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val sort: Sort = if (sortBy in listOf("brand", "model", "year")) {
            // We need to join carModel fields manually
            Sort.by(sortOrd, "carModel.$sortBy")
        } else {
            // Sorting on Vehicle fields
            Sort.by(sortOrd, sortBy)
        }
        val pageable: Pageable = PageRequest.of(page, size, sort)
        val pageResult = vehicleRepository.findAll(spec, pageable)

        return PagedResDTO(
            currentPage = pageResult.number,
            totalPages = pageResult.totalPages,
            totalElements = pageResult.totalElements,
            elementsInPage = pageResult.numberOfElements,
            content = pageResult.content.map { it.toResDTO() }
        )
    }

    override fun getVehicleById(vehicleId: Long): Vehicle {
        return vehicleRepository.findById(vehicleId).orElseThrow {
            FailureException(ResponseEnum.VEHICLE_NOT_FOUND, "Vehicle with id $vehicleId not found")
        }
    }

    override fun getAvailableVehicles(
        carModelId: Long,
        desiredStart: LocalDateTime,
        desiredEnd: LocalDateTime,
        page: Int,
        size: Int,
        sortBy: String,
        sortOrder: String
    ): PagedResDTO<VehicleResDTO> {
        val carModel: CarModel = carModelRepository.findById(carModelId).orElseThrow {
            FailureException(ResponseEnum.CAR_MODEL_NOT_FOUND, "Car model with ID $carModelId not found")
        }
        val sortOrd: Sort.Direction = if (sortOrder == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val pageResult = vehicleRepository.findAvailableVehiclesByModelAndDateRange(
            carModel = carModel,
            desiredStartWithBuffer = desiredStart.minusDays(reservationBufferDays),
            desiredEndWithBuffer = desiredEnd.plusDays(reservationBufferDays),
            desiredStart = desiredStart,
            desiredEnd = desiredEnd,
            pageable = PageRequest.of(page, size, sortOrd, sortBy)
        )
        return PagedResDTO(
            currentPage = pageResult.number,
            totalPages = pageResult.totalPages,
            totalElements = pageResult.totalElements,
            elementsInPage = pageResult.numberOfElements,
            content = pageResult.content.map { it.toResDTO() }
        )
    }

    override fun addVehicle(@Valid vehicle: VehicleReqDTO): VehicleResDTO {
        if (vehicleRepository.existsByLicensePlate(vehicle.licensePlate)) {
            throw FailureException(
                ResponseEnum.VEHICLE_DUPLICATED,
                "Vehicle with license plate ${vehicle.licensePlate} already exists"
            )
        }
        if (vehicleRepository.existsByVin(vehicle.vin)) {
            throw FailureException(ResponseEnum.VEHICLE_DUPLICATED, "Vehicle with vin ${vehicle.vin} already exists")
        }
        val carModel: CarModel = carModelRepository.findById(vehicle.carModelId).orElseThrow {
            FailureException(
                ResponseEnum.CAR_MODEL_NOT_FOUND, "Car model with ID ${vehicle.carModelId} not found"
            )
        }
        return vehicleRepository.save(vehicle.toEntity(carModel)).toResDTO()
    }

    override fun updateVehicle(vehicleId: Long, @Valid vehicle: VehicleUpdateReqDTO): VehicleResDTO {
        // Ensure vehicle with given id exists
        val vehicleToUpdate = getVehicleById(vehicleId)
        // Update properties
        vehicleToUpdate.licensePlate = vehicle.licensePlate ?: vehicleToUpdate.licensePlate
        vehicleToUpdate.kmTravelled = vehicle.kmTravelled
        vehicleToUpdate.pendingCleaning = vehicle.pendingCleaning ?: false
        vehicleToUpdate.pendingRepair = vehicle.pendingRepair ?: false
        return vehicleToUpdate.toResDTO()
    }

    override fun deleteVehicle(vehicleId: Long) {
        val vehicleToDelete = getVehicleById(vehicleId)
        vehicleRepository.delete(vehicleToDelete)
    }

    override fun deleteAllByCarModelId(carModelId: Long) {
        vehicleRepository.deleteAllByCarModelId(carModelId)
    }
}
package com.rentalcarsystem.reservationservice.services

import com.rentalcarsystem.reservationservice.dtos.request.MaintenanceReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.toEntity
import com.rentalcarsystem.reservationservice.dtos.response.MaintenanceResDTO
import com.rentalcarsystem.reservationservice.dtos.response.PagedResDTO
import com.rentalcarsystem.reservationservice.dtos.response.toResDTO
import com.rentalcarsystem.reservationservice.enums.CarStatus
import com.rentalcarsystem.reservationservice.enums.MaintenanceType
import com.rentalcarsystem.reservationservice.exceptions.FailureException
import com.rentalcarsystem.reservationservice.exceptions.ResponseEnum
import com.rentalcarsystem.reservationservice.filters.MaintenanceFilter
import com.rentalcarsystem.reservationservice.models.Maintenance
import com.rentalcarsystem.reservationservice.models.Vehicle
import com.rentalcarsystem.reservationservice.repositories.MaintenanceRepository
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Service
@Validated
@Transactional
class MaintenanceServiceImpl(
    private val maintenanceRepository: MaintenanceRepository,
    private val vehicleService: VehicleService
) : MaintenanceService {

    override fun getMaintenances(
        vehicleId: Long,
        page: Int,
        size: Int,
        sortBy: String,
        sortOrder: String,
        @Valid filters: MaintenanceFilter
    ): PagedResDTO<MaintenanceResDTO> {
        val vehicle = vehicleService.getVehicleById(vehicleId)
        var spec: Specification<Maintenance> = Specification.where(null)
        // Defects
        filters.defects?.takeIf { it.isNotBlank() }?.let { defects ->
            spec = spec.and { root, _, cb ->
                cb.like(cb.lower(root.get("defects")), "${defects.lowercase()}%")
            }
        }
        // Completed
        filters.completed?.let { completed ->
            spec = spec.and { root, _, cb ->
                cb.equal(root.get<Boolean>("completed"), completed)
            }
        }
        // Type
        filters.type?.let { type ->
            spec = spec.and { root, _, cb ->
                cb.equal(root.get<MaintenanceType>("type"), type)
            }
        }
        // Upcoming Service Needs
        filters.upcomingServiceNeeds?.takeIf { it.isNotBlank() }?.let { upcomingServiceNeeds ->
            spec = spec.and { root, _, cb ->
                cb.like(cb.lower(root.get("upcomingServiceNeeds")), "${upcomingServiceNeeds.lowercase()}%")
            }
        }
        // startDate range
        filters.minStartDate?.let { minStartDate ->
            spec = spec.and { root, _, cb ->
                cb.greaterThanOrEqualTo(root.get("startDate"), minStartDate)
            }
        }
        filters.maxStartDate?.let { maxStartDate ->
            spec = spec.and { root, _, cb ->
                cb.lessThanOrEqualTo(root.get("startDate"), maxStartDate)
            }
        }
        // plannedEndDate range
        filters.minPlannedEndDate?.let { minPlannedEndDate ->
            spec = spec.and { root, _, cb ->
                cb.greaterThanOrEqualTo(root.get("plannedEndDate"), minPlannedEndDate)
            }
        }
        filters.maxPlannedEndDate?.let { maxPlannedEndDate ->
            spec = spec.and { root, _, cb ->
                cb.lessThanOrEqualTo(root.get("plannedEndDate"), maxPlannedEndDate)
            }
        }
        // actualEndDate range
        filters.minActualEndDate?.let { minActualEndDate ->
            spec = spec.and { root, _, cb ->
                cb.greaterThanOrEqualTo(root.get("actualEndDate"), minActualEndDate)
            }
        }
        filters.maxActualEndDate?.let { maxActualEndDate ->
            spec = spec.and { root, _, cb ->
                cb.lessThanOrEqualTo(root.get("actualEndDate"), maxActualEndDate)
            }
        }
        // fleetManagerUsername
        filters.fleetManagerUsername?.takeIf { it.isNotBlank() }?.let { fleetManagerUsername ->
            spec = spec.and { root, _, cb ->
                cb.like(cb.lower(root.get("fleetManagerUsername")), "${fleetManagerUsername.lowercase()}%")
            }
        }
        // Vehicle
        spec = spec.and { root, _, cb ->
            cb.equal(root.get<Vehicle>("vehicle"), vehicle)
        }
        // Sorting
        val sortOrd: Sort.Direction = if (sortOrder == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val pageable: Pageable = PageRequest.of(page, size, sortOrd, sortBy)
        val pageResult = maintenanceRepository.findAll(spec, pageable)

        return PagedResDTO(
            currentPage = pageResult.number,
            totalPages = pageResult.totalPages,
            totalElements = pageResult.totalElements,
            elementsInPage = pageResult.numberOfElements,
            content = pageResult.content.map { it.toResDTO() }
        )
    }

    override fun getMaintenanceById(vehicleId: Long, maintenanceId: Long): MaintenanceResDTO {
        val maintenance = getActualMaintenanceById(maintenanceId)
        checkMaintenanceVehicleMatch(vehicleId, maintenance)
        return maintenance.toResDTO()
    }

    override fun createMaintenance(
        vehicleId: Long,
        @Valid maintenanceReq: MaintenanceReqDTO,
        username: String
    ): MaintenanceResDTO {
        val vehicle = vehicleService.getVehicleById(vehicleId)
        val maintenance = maintenanceReq.toEntity(username)
        if (maintenance.completed) {
            throw IllegalArgumentException("A newly created maintenance record cannot be completed")
        }
        vehicle.addMaintenance(maintenance)
        // Set vehicle status to maintenance
        vehicle.status = CarStatus.IN_MAINTENANCE
        return maintenanceRepository.save(maintenance).toResDTO()
    }

    override fun updateMaintenance(
        vehicleId: Long,
        maintenanceId: Long,
        @Valid maintenanceReq: MaintenanceReqDTO,
    ): MaintenanceResDTO {
        // Check if maintenance record exists
        val vehicle = vehicleService.getVehicleById(vehicleId)
        val maintenance = getActualMaintenanceById(maintenanceId)
        checkMaintenanceVehicleMatch(vehicleId, maintenance)
        // Check maintenance status
        if (maintenance.completed) {
            throw IllegalArgumentException("Maintenance record with ID $maintenanceId is already completed")
        }
        // Check if incoming request will close the maintenance
        if (maintenanceReq.completed) {
            vehicle.status = CarStatus.AVAILABLE
        }
        maintenance.defects = maintenanceReq.defects
        maintenance.completed = maintenanceReq.completed
        maintenance.type = maintenanceReq.type
        maintenance.upcomingServiceNeeds = maintenanceReq.upcomingServiceNeeds
        maintenance.startDate = maintenanceReq.startDate
        maintenance.plannedEndDate = maintenanceReq.plannedEndDate
        maintenance.actualEndDate = maintenanceReq.actualEndDate ?: maintenance.actualEndDate
        return maintenance.toResDTO()
    }

    override fun deleteMaintenance(vehicleId: Long, maintenanceId: Long) {
        val vehicle = vehicleService.getVehicleById(vehicleId)
        val maintenance = getActualMaintenanceById(maintenanceId)
        checkMaintenanceVehicleMatch(vehicleId, maintenance)
        vehicle.removeMaintenance(maintenance)
    }

    override fun getActualMaintenanceById(maintenanceId: Long): Maintenance {
        return maintenanceRepository.findById(maintenanceId).orElseThrow {
            FailureException(ResponseEnum.MAINTENANCE_NOT_FOUND, "Maintenance record with id $maintenanceId not found")
        }
    }

    private fun checkMaintenanceVehicleMatch(vehicleId: Long, maintenance: Maintenance) {
        if (maintenance.vehicle?.getId() != vehicleId) {
            throw IllegalArgumentException("Maintenance record with ID ${maintenance.getId()} does not belong to vehicle with ID $vehicleId")
        }
    }
}
package com.rentalcarsystem.reservationservice.services

import com.rentalcarsystem.reservationservice.dtos.request.FinalizeMaintenanceReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.MaintenanceReqDTO
import com.rentalcarsystem.reservationservice.dtos.response.MaintenanceResDTO
import com.rentalcarsystem.reservationservice.dtos.response.PagedResDTO
import com.rentalcarsystem.reservationservice.filters.MaintenanceFilter
import com.rentalcarsystem.reservationservice.models.Maintenance
import jakarta.validation.Valid

interface MaintenanceService {
    fun getMaintenances(
        vehicleId: Long,
        page: Int,
        size: Int,
        sortBy: String,
        sortOrder: String,
        @Valid filters: MaintenanceFilter
    ): PagedResDTO<MaintenanceResDTO>

    fun getMaintenanceById(vehicleId: Long, maintenanceId: Long): MaintenanceResDTO

    fun getActualMaintenanceById(maintenanceId: Long): Maintenance

    fun createMaintenance(vehicleId: Long, @Valid maintenanceReq: MaintenanceReqDTO, username: String): MaintenanceResDTO

    fun finalizeMaintenance(
        vehicleId: Long,
        maintenanceId: Long,
        @Valid finalizeMaintenanceReq: FinalizeMaintenanceReqDTO,
        username: String
    ): MaintenanceResDTO

    fun updateMaintenance(
        vehicleId: Long,
        maintenanceId: Long,
        @Valid maintenanceReq: MaintenanceReqDTO
    ): MaintenanceResDTO

    fun deleteMaintenance(vehicleId: Long, maintenanceId: Long)
}
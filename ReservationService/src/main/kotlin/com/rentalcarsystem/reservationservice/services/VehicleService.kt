package com.rentalcarsystem.reservationservice.services

import com.rentalcarsystem.reservationservice.dtos.request.VehicleReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.VehicleUpdateReqDTO
import com.rentalcarsystem.reservationservice.dtos.response.PagedResDTO
import com.rentalcarsystem.reservationservice.dtos.response.VehicleResDTO
import com.rentalcarsystem.reservationservice.filters.VehicleFilter
import com.rentalcarsystem.reservationservice.models.Vehicle
import jakarta.validation.Valid
import java.time.LocalDateTime

interface VehicleService {
    fun getVehicles(
        page: Int,
        size: Int,
        sortBy: String,
        sortOrder: String,
        @Valid filters: VehicleFilter,
    ): PagedResDTO<VehicleResDTO>

    fun getVehicleById(vehicleId: Long): Vehicle
    fun getAvailableVehicles(
        carModelId: Long,
        desiredStart: LocalDateTime,
        desiredEnd: LocalDateTime,
        page: Int,
        size: Int,
        sortBy: String,
        sortOrder: String
    ): PagedResDTO<VehicleResDTO>

    fun addVehicle(@Valid vehicle: VehicleReqDTO): VehicleResDTO
    fun updateVehicle(vehicleId: Long, @Valid vehicle: VehicleUpdateReqDTO): VehicleResDTO
    fun deleteVehicle(vehicleId: Long)
    fun deleteAllByCarModelId(carModelId: Long)
}
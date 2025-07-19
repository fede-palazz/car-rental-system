package com.rentalcarsystem.reservationservice.services

import com.rentalcarsystem.reservationservice.dtos.request.CarModelReqDTO
import com.rentalcarsystem.reservationservice.dtos.response.CarFeatureResDTO
import com.rentalcarsystem.reservationservice.dtos.response.CarModelResDTO
import com.rentalcarsystem.reservationservice.dtos.response.PagedResDTO
import com.rentalcarsystem.reservationservice.filters.CarModelFilter
import com.rentalcarsystem.reservationservice.models.CarModel
import jakarta.validation.Valid
import java.time.LocalDateTime

interface CarModelService {
    fun getAllModels(
        page: Int,
        size: Int,
        singlePage: Boolean,
        sortBy: String,
        sortOrder: String,
        @Valid filters: CarModelFilter,
        desiredPickUpDate: LocalDateTime? = null,
        desiredDropOffDate: LocalDateTime? = null,
        customerUsername: String? = null,
        isCustomer: Boolean,
        reservationToUpdateId: Long? = null
    ): PagedResDTO<CarModelResDTO>

    fun getCarModelById(id: Long): CarModelResDTO
    fun getAllCarFeatures(): List<CarFeatureResDTO>
    fun getCarFeatureById(id: Long): CarFeatureResDTO
    fun getCarFeaturesByCarModelId(carModelId: Long): List<CarFeatureResDTO>
    fun createCarModel(@Valid model: CarModelReqDTO): CarModelResDTO
    fun updateCarModel(id: Long, @Valid model: CarModelReqDTO): CarModelResDTO
    fun deleteCarModelById(id: Long)
    fun getActualCarModelById(id: Long): CarModel
}
package com.rentalcarsystem.trackingservice.dtos.response

data class PagedResDTO<T>(
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Long,
    val elementsInPage: Int,
    val content: List<T>
)
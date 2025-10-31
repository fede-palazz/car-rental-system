package com.rentalcarsystem.reservationservice.services

import com.rentalcarsystem.reservationservice.dtos.request.NoteReqDTO
import com.rentalcarsystem.reservationservice.dtos.response.NoteResDTO
import com.rentalcarsystem.reservationservice.dtos.response.PagedResDTO
import com.rentalcarsystem.reservationservice.filters.NoteFilter
import jakarta.validation.Valid

interface NoteService {
    fun getNotes(
        page: Int,
        size: Int,
        sortBy: String,
        sortOrder: String,
        vehicleId: Long,
        @Valid filters: NoteFilter,
    ): PagedResDTO<NoteResDTO>

    fun getNote(noteId: Long): NoteResDTO

    fun createNote(
        vehicleId: Long,
        @Valid noteReq: NoteReqDTO,
        username: String
    ): NoteResDTO

    fun updateNote(
        vehicleId: Long,
        noteId: Long,
        @Valid noteReq: NoteReqDTO,
        username: String
    ): NoteResDTO

    fun deleteNote(
        vehicleId: Long,
        noteId: Long,
        username: String
    )
}
package com.rentalcarsystem.reservationservice.services

import com.rentalcarsystem.reservationservice.dtos.request.NoteReqDTO
import com.rentalcarsystem.reservationservice.dtos.request.toEntity
import com.rentalcarsystem.reservationservice.dtos.response.NoteResDTO
import com.rentalcarsystem.reservationservice.dtos.response.PagedResDTO
import com.rentalcarsystem.reservationservice.dtos.response.toResDTO
import com.rentalcarsystem.reservationservice.exceptions.FailureException
import com.rentalcarsystem.reservationservice.exceptions.ResponseEnum
import com.rentalcarsystem.reservationservice.filters.NoteFilter
import com.rentalcarsystem.reservationservice.models.Note
import com.rentalcarsystem.reservationservice.models.Vehicle
import com.rentalcarsystem.reservationservice.repositories.NoteRepository
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
class NoteServiceImpl(
    private val noteRepository: NoteRepository,
    private val vehicleService: VehicleService
) : NoteService {
    override fun getNotes(
        page: Int,
        size: Int,
        sortBy: String,
        sortOrder: String,
        vehicleId: Long,
        @Valid filters: NoteFilter
    ): PagedResDTO<NoteResDTO> {
        val vehicle = vehicleService.getVehicleById(vehicleId)
        var spec: Specification<Note> = Specification.where(null)
        // Content
        filters.content?.takeIf { it.isNotBlank() }?.let { content ->
            spec = spec.and { root, _, cb ->
                cb.like(cb.lower(root.get("content")), "%${content.lowercase()}%")
            }
        }
        // Author
        filters.author?.takeIf { it.isNotBlank() }?.let { author ->
            spec = spec.and { root, _, cb ->
                cb.like(cb.lower(root.get("author")), "%${author.lowercase()}%")
            }
        }
        // Date
        filters.minDate?.let { minDate ->
            spec = spec.and { root, _, cb ->
                cb.greaterThanOrEqualTo(root.get("date"), minDate)
            }
        }
        filters.maxDate?.let { maxDate ->
            spec = spec.and { root, _, cb ->
                cb.lessThanOrEqualTo(root.get("date"), maxDate)
            }
        }
        // Vehicle
        spec = spec.and { root, _, cb ->
            cb.equal(root.get<Vehicle>("vehicle"), vehicle)
        }
        // Sorting
        val sortOrd: Sort.Direction = if (sortOrder == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val pageable: Pageable = PageRequest.of(page, size, sortOrd, sortBy)
        val pageResult = noteRepository.findAll(spec, pageable)

        return PagedResDTO(
            currentPage = pageResult.number,
            totalPages = pageResult.totalPages,
            totalElements = pageResult.totalElements,
            elementsInPage = pageResult.numberOfElements,
            content = pageResult.content.map { it.toResDTO() }
        )
    }

    override fun createNote(vehicleId: Long, @Valid noteReq: NoteReqDTO): NoteResDTO {
        // Get the corresponding vehicle
        val vehicle = vehicleService.getVehicleById(vehicleId)
        val noteToSave = noteReq.toEntity()
        vehicle.addNote(noteToSave)
        return noteRepository.save(noteToSave).toResDTO()
    }

    fun getNoteById(id: Long): Note {
        return noteRepository.findById(id)
            .orElseThrow { FailureException(ResponseEnum.NOTE_NOT_FOUND, "Note with ID $id not found") }
    }

    override fun updateNote(id: Long, vehicleId: Long, @Valid noteReq: NoteReqDTO): NoteResDTO {
        val vehicle = vehicleService.getVehicleById(vehicleId)
        val note = getNoteById(id)
        // Ensure the note belongs to the correct vehicle
        if (note.vehicle?.getId() != vehicle.getId()) {
            throw IllegalArgumentException("Note with ID $id does not belong to vehicle with ID $vehicleId")
        }
        // Update the fields
        note.content = noteReq.content
        note.author = noteReq.author
        note.date = noteReq.date ?: note.date
        return note.toResDTO()
    }

    override fun deleteNote(vehicleId: Long, noteId: Long) {
        val vehicle = vehicleService.getVehicleById(vehicleId)
        val note = getNoteById(noteId)
        // Ensure the note belongs to the correct vehicle
        if (note.vehicle?.getId() != vehicleId) {
            throw IllegalArgumentException("Note with ID $noteId does not belong to vehicle with ID $vehicleId")
        }
        // Remove note from both sides
        vehicle.removeNote(note)
    }
}
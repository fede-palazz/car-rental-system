package com.rentalcarsystem.reservationservice.dtos.request

import com.rentalcarsystem.reservationservice.models.Note
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class NoteReqDTO(
    @field:NotBlank(message = "Content must not be blank")
    val content: String,
    @field:NotBlank(message = "Author must not be blank")
    val author: String,
    val date: LocalDateTime?,
)

fun NoteReqDTO.toEntity() = Note(
    content = this.content,
    author = this.author,
    date = this.date ?: LocalDateTime.now(),
)
package com.rentalcarsystem.reservationservice.dtos.request

import com.rentalcarsystem.reservationservice.models.Note
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime
import java.time.ZoneOffset

data class NoteReqDTO(
    @field:NotBlank(message = "Content must not be blank")
    val content: String,
)

fun NoteReqDTO.toEntity(username: String) = Note(
    content = this.content,
    author = username,
    date = LocalDateTime.now(ZoneOffset.UTC),
)
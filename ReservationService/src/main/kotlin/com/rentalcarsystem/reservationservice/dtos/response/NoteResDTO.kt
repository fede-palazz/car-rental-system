package com.rentalcarsystem.reservationservice.dtos.response

import com.rentalcarsystem.reservationservice.models.Note
import java.time.LocalDateTime

data class NoteResDTO(
    val id: Long,
    val content: String,
    val author: String,
    val date: LocalDateTime
)

fun Note.toResDTO() = NoteResDTO(
    this.getId()!!,
    content,
    author,
    date
)

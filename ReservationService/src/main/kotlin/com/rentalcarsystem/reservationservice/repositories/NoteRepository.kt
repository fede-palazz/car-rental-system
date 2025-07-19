package com.rentalcarsystem.reservationservice.repositories

import com.rentalcarsystem.reservationservice.models.Note
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface NoteRepository : JpaRepository<Note, Long>, JpaSpecificationExecutor<Note>
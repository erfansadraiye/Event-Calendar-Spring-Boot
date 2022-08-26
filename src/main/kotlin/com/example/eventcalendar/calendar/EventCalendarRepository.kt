package com.example.eventcalendar.calendar

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface EventCalendarRepository : JpaRepository<Task, Long?> {

    fun findTaskByState(state: TaskState): Optional<List<Task>>

    fun findTasksByDeadlineLessThanEqual(deadline: LocalDate): Optional<List<Task>>

    fun findTasksByTitleContainsIgnoreCase(title: String): Optional<List<Task>>

    fun findTaskByTitleEquals(title: String): Optional<Task>

    fun deleteTaskByStateEquals(state: TaskState): Unit

    fun existsByTitle(title: String) : Boolean
}
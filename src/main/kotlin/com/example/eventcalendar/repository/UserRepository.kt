package com.example.eventcalendar.repository

import com.example.eventcalendar.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long?> {

    fun findByEmail(email: String): Optional<User>

    fun existsByEmail(email: String): Boolean

    fun findUsersByAssignedTasksId(taskId : Long) : Optional<List<User>>

}
package com.example.eventcalendar.service

import com.example.eventcalendar.model.User
import com.example.eventcalendar.repository.EventCalendarRepository
import com.example.eventcalendar.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import javax.transaction.Transactional


@Service
class UserService(userRepo: UserRepository) : IntUserService {

    private val userRepository: UserRepository = userRepo

    @Autowired
    lateinit var eventCalendarRepository: EventCalendarRepository

    override fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }

    override fun getUserById(id: Long): User {
        val userOptional = userRepository.findById(id)
        if (!userOptional.isPresent) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "no user with id $id ")
        }
        return userOptional.get()
    }

    override fun getUserByEmail(email: String): User {
        val userOptional = userRepository.findByEmail(email)
        if (!userOptional.isPresent) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "no user with email $email ")
        }
        return userOptional.get()
    }

    override fun addUser(user: User): Long? {
        if (user.id != null)
            if (userRepository.existsById(user.id!!)) throw ResponseStatusException(
                HttpStatus.CONFLICT, "id is duplicate!"
            )
        if (userRepository.existsByEmail(user.email!!)) throw ResponseStatusException(
            HttpStatus.CONFLICT, "email is duplicate!"
        )
        return userRepository.save(user).id
    }

    @Transactional
    override fun updateEmailById(id: Long, email: String): User {
        val userOptional = userRepository.findById(id)
        if (!userOptional.isPresent) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "no user with id $id")
        }
        val user = userOptional.get()
        if (userRepository.existsByEmail(email))
            throw ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "email is duplicate!")
        user.email = email
        return user
    }

    @Transactional
    override fun updateNameById(id: Long, firstname: String?, lastname: String?): User {
        val userOptional = userRepository.findById(id)
        if (!userOptional.isPresent) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "no user with id $id")
        }
        val user = userOptional.get()
        if (firstname != null && firstname != user.firstName)
            user.firstName = firstname
        if (lastname != null && lastname != user.lastName)
            user.lastName = lastname
        return user
    }

    override fun getUsersByTaskId(taskId: Long?): List<User> {
        val taskOptional = eventCalendarRepository.findById(taskId!!)
        if (!taskOptional.isPresent) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "no task with id $taskId ")
        }
        return userRepository.findUsersByAssignedTasksId(taskId).get()
    }

    override fun deleteUserById(id: Long): Boolean {
        val userOptional = userRepository.findById(id)
        if (!userOptional.isPresent) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND, "no user with id $id "
            )
        }
        userRepository.deleteById(id)
        return true
    }

}
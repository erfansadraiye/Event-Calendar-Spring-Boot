package com.example.eventcalendar.service

import com.example.eventcalendar.model.User
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/calendar/users/")
interface IntUserService {
    @GetMapping("list")
    fun getAllUsers(): List<User>

    @GetMapping("id/{userId}")
    fun getUserById(@PathVariable("userId") id: Long): User

    @GetMapping("email/{email}")
    fun getUserByEmail(@PathVariable("email") email: String): User


    @GetMapping("get_users_by_task/task_id/{id}")
    fun getUsersByTaskId(@PathVariable("id") taskId: Long?): List<User>

    @PostMapping("create", consumes = ["application/json"])
    @ResponseStatus(HttpStatus.CREATED)
    fun addUser(@RequestBody user: User): Long?

    @PutMapping("change_email/{id}")
    fun updateEmailById(
        @PathVariable("id") id: Long,
        @RequestParam email: String
    ): User

    @PutMapping("change_name/{id}")
    fun updateNameById(
        @PathVariable("id") id: Long,
        @RequestParam(required = false) firstname: String?,
        @RequestParam(required = false) lastname: String?
    ): User

    @DeleteMapping("remove_user/{id}")
    fun deleteUserById(@PathVariable("id") id: Long): Boolean

}
package com.example.eventcalendar.service

import com.example.eventcalendar.model.Task
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/calendar/tasks/")
interface IntEventCalendarService {

    @GetMapping("list")
    fun getAllTasks(): List<Task>

    @GetMapping("id/{taskId}")
    fun getTaskById(@PathVariable("taskId") id: Long): Task

    @GetMapping("title/{title}")
    fun getTaskByTitle(@PathVariable("title") title: String): Task

    @GetMapping("search_title/{title}")
    fun getTasksBySearchTitle(@PathVariable("title") title: String): List<Task>

    @GetMapping("state/{state}")
    fun getTasksByState(@PathVariable("state") state: String): List<Task>

    @GetMapping("deadline/{deadline}")
    fun getTasksUntilDeadline(@PathVariable("deadline") deadline: String): List<Task>

    @PostMapping("add_task", consumes = ["application/json"])
    @ResponseStatus(HttpStatus.CREATED)
    fun addTask(@RequestBody task: Task): Long?

    @PutMapping("change_state/id/{taskId}")
    fun updateStateById(
        @PathVariable("taskId") id: Long,
        @RequestParam state: String
    ): Task

    @PutMapping("change_state/title/{taskTitle}")
    fun updateStateByTitle(
        @PathVariable("taskTitle") title: String,
        @RequestParam state: String
    ): Task

    @PutMapping("change_deadline/id/{id}", consumes = ["application/json"])
    fun updateDeadlineById(
        @PathVariable("id") id: Long,
        @RequestParam deadline: String
    ): Task

    @PutMapping("change_deadline/title/{title}", consumes = ["application/json"])
    fun updateDeadlineByTitle(
        @PathVariable("title") title: String,
        @RequestParam deadline: String
    ): Task


    @PutMapping("assign/{taskID}", consumes = ["application/json"])
    fun assignTask(
        @PathVariable("taskID") taskID: Long,
        @RequestParam userid: Long?
    ): Boolean


    @DeleteMapping("remove_task/{taskId}")
    fun deleteTask(@PathVariable("taskId") id: Long): Boolean

    @DeleteMapping("clear_done_tasks")
    fun clearDoneTasks(): Boolean

    @GetMapping("get_tasks_by_user/user_id/{id}")
    fun getTasksByUserId(@PathVariable("id") id: Long?): List<Task>
}

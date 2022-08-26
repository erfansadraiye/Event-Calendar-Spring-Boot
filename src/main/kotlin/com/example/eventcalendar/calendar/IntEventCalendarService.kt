package com.example.eventcalendar.calendar

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/calendar")
interface IntEventCalendarService {

    @GetMapping("tasks")
    fun getAllTasks(): ResponseEntity<*>

    @GetMapping("tasks/id/{taskId}")
    fun getTaskById(@PathVariable("taskId") id: Long): ResponseEntity<*>

    @GetMapping("tasks/title/{title}")
    fun getTaskByTitle(@PathVariable("title") title: String): ResponseEntity<*>

    @GetMapping("tasks/search_title/{title}")
    fun getTasksBySearchTitle(@PathVariable("title") title: String): ResponseEntity<*>

    @GetMapping("tasks/state/{state}")
    fun getTasksByState(@PathVariable("state") state: String): ResponseEntity<*>

    @GetMapping("tasks/deadline/{deadline}")
    fun getTasksUntilDeadline(@PathVariable("deadline") deadline: String): ResponseEntity<*>

    @PostMapping("add_task", consumes = ["application/json"])
    fun addTask(@RequestBody task: Task): ResponseEntity<*>

    @PutMapping("change_state/id/{taskId}")
    fun updateStateById(
        @PathVariable("taskId") id: Long,
        @RequestParam state: String
    ): ResponseEntity<*>

    @PutMapping("change_state/title/{taskTitle}")
    fun updateStateByTitle(
        @PathVariable("taskTitle") title: String,
        @RequestParam state: String
    ): ResponseEntity<*>

    @PutMapping("change_deadline/id/{id}", consumes = ["application/json"])
    fun updateDeadlineById(
        @PathVariable("id") id: Long,
        @RequestParam deadline: String
    ): ResponseEntity<*>


    @PutMapping("change_deadline/title/{title}", consumes = ["application/json"])
    fun updateDeadlineByTitle(
        @PathVariable("title") title: String,
        @RequestParam deadline: String
    ): ResponseEntity<*>

    @DeleteMapping("remove_task/{taskId}")
    fun deleteTask(@PathVariable("taskId") id: Long): ResponseEntity<*>


    @DeleteMapping("clear_done_tasks")
    fun clearDoneTasks(): ResponseEntity<*>
}

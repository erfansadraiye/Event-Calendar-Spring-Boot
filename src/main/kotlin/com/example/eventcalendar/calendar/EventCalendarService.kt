package com.example.eventcalendar.calendar

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Service
class EventCalendarService(calendarRepository: EventCalendarRepository) : IntEventCalendarService {

    @Autowired
    private val eventCalendarRepository: EventCalendarRepository = calendarRepository

    override fun getAllTasks(): ResponseEntity<*> {
        return ResponseEntity(eventCalendarRepository.findAll(), HttpStatus.OK)
    }

    override fun getTaskById(id: Long): ResponseEntity<*> {
        val taskOptional = eventCalendarRepository.findById(id)
        if (taskOptional.isEmpty) {
            return ResponseEntity(ResponseError(HttpStatus.NOT_FOUND, "no task with id $id "), HttpStatus.NOT_FOUND)
        }
        return ResponseEntity(taskOptional.get(), HttpStatus.OK)
    }

    override fun getTaskByTitle(title: String): ResponseEntity<*> {
        val taskOptional = eventCalendarRepository.findTaskByTitleEquals(title)
        if (taskOptional.isEmpty) {
            return ResponseEntity(
                ResponseError(HttpStatus.NOT_FOUND, "no task with title $title "), HttpStatus.NOT_FOUND
            )
        }
        return ResponseEntity(taskOptional.get(), HttpStatus.OK)
    }

    override fun getTasksBySearchTitle(title: String): ResponseEntity<*> {
        return ResponseEntity(eventCalendarRepository.findTasksByTitleContainsIgnoreCase(title).get(), HttpStatus.OK)
    }

    override fun getTasksByState(state: String): ResponseEntity<*> {
        return try {
            val taskState = TaskState.valueOf(state)
            val events = eventCalendarRepository.findTaskByState(taskState).get()
            ResponseEntity(events, HttpStatus.OK)
        } catch (e: Exception) {
            return ResponseEntity(
                ResponseError(HttpStatus.NOT_ACCEPTABLE, e.message), HttpStatus.NOT_ACCEPTABLE
            )
        }
    }

    override fun getTasksUntilDeadline(deadline: String): ResponseEntity<*> {
        return try {
            val deadlineLocalDate = if (deadline == "today") LocalDate.now()
            else LocalDate.parse(deadline, DateTimeFormatter.ISO_DATE)
            val tasks = eventCalendarRepository.findTasksByDeadlineLessThanEqual(deadlineLocalDate!!).get()
            ResponseEntity(tasks, HttpStatus.OK)
        } catch (e: Exception) {
            return ResponseEntity(
                ResponseError(HttpStatus.NOT_ACCEPTABLE, e.message), HttpStatus.NOT_ACCEPTABLE
            )
        }
    }

    override fun addTask(task: Task): ResponseEntity<*> {
        if (task.id != null) if (eventCalendarRepository.existsById(task.id!!)) return ResponseEntity(
            ResponseError(
                HttpStatus.CONFLICT, "id is duplicate!"
            ), HttpStatus.CONFLICT
        )
        if (eventCalendarRepository.existsByTitle(task.title!!)) return ResponseEntity(
            ResponseError(
                HttpStatus.CONFLICT, "title is duplicate!"
            ), HttpStatus.CONFLICT
        )
        if (task.deadline!!.isBefore(LocalDate.now())) return ResponseEntity(
            ResponseError(HttpStatus.NOT_ACCEPTABLE, "deadline is invalid!"), HttpStatus.NOT_ACCEPTABLE
        )
        eventCalendarRepository.save(task)
        return ResponseEntity(task, HttpStatus.CREATED)

    }

    @Transactional
    override fun updateStateById(id: Long, state: String): ResponseEntity<*> {
        val taskOptional = eventCalendarRepository.findById(id)
        if (taskOptional.isEmpty) {
            return ResponseEntity(ResponseError(HttpStatus.NOT_FOUND, "no task with id $id "), HttpStatus.NOT_FOUND)
        }
        val task = taskOptional.get()
        var taskState: TaskState? = null
        try {
            taskState = TaskState.valueOf(state)
        } catch (e: Exception) {
            return ResponseEntity(
                ResponseError(HttpStatus.NOT_ACCEPTABLE, e.message), HttpStatus.NOT_ACCEPTABLE
            )
        }
        if (task.state == taskState) return ResponseEntity(
            ResponseError(HttpStatus.NOT_ACCEPTABLE, "new status and current status are same!"),
            HttpStatus.NOT_ACCEPTABLE
        )
        task.state = taskState
        return ResponseEntity(task, HttpStatus.OK)
    }

    @Transactional
    override fun updateStateByTitle(title: String, state: String): ResponseEntity<*> {
        val taskOptional = eventCalendarRepository.findTaskByTitleEquals(title)
        if (taskOptional.isEmpty) return ResponseEntity(
            ResponseError(HttpStatus.NOT_FOUND, "event with title=$title doesn't exist!"), HttpStatus.NOT_FOUND
        )
        val task = taskOptional.get()
        var taskState: TaskState? = null
        try {
            taskState = TaskState.valueOf(state)
        } catch (e: Exception) {
            return ResponseEntity(
                ResponseError(HttpStatus.NOT_ACCEPTABLE, e.message), HttpStatus.NOT_ACCEPTABLE
            )
        }
        if (task.state == taskState) return ResponseEntity(
            ResponseError(HttpStatus.NOT_ACCEPTABLE, "new status and current status are same!"),
            HttpStatus.NOT_ACCEPTABLE
        )
        task.state = taskState
        return ResponseEntity(task, HttpStatus.OK)
    }

    @Transactional
    override fun updateDeadlineById(id: Long, deadline: String): ResponseEntity<*> {
        val taskOptional = eventCalendarRepository.findById(id)
        if (taskOptional.isEmpty) {
            return ResponseEntity(ResponseError(HttpStatus.NOT_FOUND, "no task with id $id "), HttpStatus.NOT_FOUND)
        }
        val task = taskOptional.get()
        var deadlineLocalDate: LocalDate? = null
        try {
            deadlineLocalDate = LocalDate.parse(deadline, DateTimeFormatter.ISO_DATE)
        } catch (e: Exception) {
            return ResponseEntity(
                ResponseError(HttpStatus.NOT_ACCEPTABLE, e.message), HttpStatus.NOT_ACCEPTABLE
            )
        }
        if (task.deadline == deadlineLocalDate) return ResponseEntity(
            ResponseError(
                HttpStatus.NOT_ACCEPTABLE, "new deadline and current deadline are same!"
            ), HttpStatus.NOT_ACCEPTABLE
        )
        task.deadline = deadlineLocalDate
        return ResponseEntity(task, HttpStatus.OK)
    }

    @Transactional
    override fun updateDeadlineByTitle(title: String, deadline: String): ResponseEntity<*> {
        val taskOptional = eventCalendarRepository.findTaskByTitleEquals(title)
        if (taskOptional.isEmpty) {
            return ResponseEntity(
                ResponseError(HttpStatus.NOT_FOUND, "no task with title $title "),
                HttpStatus.NOT_FOUND
            )
        }
        val task = taskOptional.get()
        var deadlineLocalDate: LocalDate? = null
        try {
            deadlineLocalDate = LocalDate.parse(deadline, DateTimeFormatter.ISO_DATE)
        } catch (e: Exception) {
            return ResponseEntity(
                ResponseError(HttpStatus.NOT_ACCEPTABLE, e.message), HttpStatus.NOT_ACCEPTABLE
            )
        }
        if (task.deadline == deadlineLocalDate) return ResponseEntity(
            ResponseError(
                HttpStatus.NOT_ACCEPTABLE, "new deadline and current deadline are same!"
            ), HttpStatus.NOT_ACCEPTABLE
        )
        task.deadline = deadlineLocalDate
        return ResponseEntity(task, HttpStatus.OK)
    }

    override fun deleteTask(id: Long): ResponseEntity<*> {
        val task = eventCalendarRepository.findById(id)
        if (task.isEmpty) {
            return ResponseEntity(ResponseError(HttpStatus.NOT_FOUND, "no task with id $id "), HttpStatus.NOT_FOUND)
        }
        eventCalendarRepository.deleteById(id)
        return ResponseEntity(task.get(), HttpStatus.OK)
    }

    @Transactional
    override fun clearDoneTasks(): ResponseEntity<*> {
        eventCalendarRepository.deleteTaskByStateEquals(TaskState.DONE)
        return ResponseEntity(ResponseError(HttpStatus.OK, "Done events deleted successfully"), HttpStatus.OK)
    }
}

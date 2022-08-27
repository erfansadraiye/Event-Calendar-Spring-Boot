package com.example.eventcalendar.calendar

import com.example.eventcalendar.user.User
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "tasks")
class Task(
    id: Long?,
    title: String?,
    description: String?,
    deadline: LocalDate?
) {
    @Id
    @GeneratedValue(
        strategy = GenerationType.IDENTITY
    )
    var id: Long? = id

    @Column(unique = true)
    var title: String? = title
    var description: String? = description
    var deadline: LocalDate? = deadline

    @Enumerated(value = EnumType.STRING)
    var state: TaskState? = TaskState.TO_DO
    @ManyToMany(mappedBy = "assignedTasks")
    var members : Set<User> = setOf()
    constructor(title: String, description: String, deadline: LocalDate) :
            this(null, title, description, deadline) {

    }

    constructor() : this(null, null, null, null) {

    }

    override fun equals(other: Any?): Boolean {
        if (other is Task) {
            return id == other.id && title == other.title
        }
        return false
    }
}
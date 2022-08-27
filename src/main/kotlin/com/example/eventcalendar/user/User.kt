package com.example.eventcalendar.user

import com.example.eventcalendar.calendar.Task
import javax.persistence.*

@Entity
@Table(name = "users")
class User(
    id: Long?, firstName: String?, lastName: String?, email: String?
) {
    @Id
    @Column(unique = true)
    @GeneratedValue(
        strategy = GenerationType.IDENTITY
    )
    var id: Long? = id
    var firstName: String? = firstName
    var lastName: String? = lastName

    @Column(unique = true)
    var email: String? = email

    @ManyToMany(targetEntity = Task::class)
    @JoinTable(
        name = "users_tasks",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "task_id")]
    )
    var assignedTasks: List<Task>? = null

    constructor() : this(null, null, null, null) {

    }

    constructor(firstName: String?, lastName: String?, email: String?) : this(null, firstName, lastName, email) {}

    override fun equals(other: Any?): Boolean {
        if (other is User) {
            return id == other.id && email == other.email
        }
        return false
    }
}
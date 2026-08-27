package org.example.todoapp.habit

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.example.todoapp.common.BaseEntity
import org.example.todoapp.goal.Goal
import java.time.LocalDate

@Entity
@Table(
    name = "habit_entries",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_habit_entries_goal_date", columnNames = ["goal_id", "date"]),
    ],
)
class HabitEntry(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    var goal: Goal,
    @Column(name = "date", nullable = false)
    var date: LocalDate,
    @Column(nullable = false)
    var done: Boolean = false,
) : BaseEntity()

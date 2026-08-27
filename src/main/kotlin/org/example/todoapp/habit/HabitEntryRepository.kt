package org.example.todoapp.habit

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface HabitEntryRepository : JpaRepository<HabitEntry, Long> {
    fun findByGoalIdAndDate(goalId: Long, date: LocalDate): HabitEntry?

    fun findByGoalIdAndDateBetweenOrderByDateDesc(goalId: Long, from: LocalDate, to: LocalDate): List<HabitEntry>
}

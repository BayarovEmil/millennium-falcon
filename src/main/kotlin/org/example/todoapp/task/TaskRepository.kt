package org.example.todoapp.task

import org.example.todoapp.period.PeriodType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface TaskRepository : JpaRepository<Task, Long> {
    fun findByPeriodId(periodId: Long): List<Task>

    fun findByPeriodIdOrderBySortOrderAsc(periodId: Long): List<Task>

    fun findByPeriod_TypeAndPeriod_StartDateBetween(type: PeriodType, from: LocalDate, to: LocalDate): List<Task>

    /** All tasks scoped directly to this period, plus tasks under any of its child (DAY) periods. */
    @Query("select t from Task t where t.period.id = :periodId or t.period.parent.id = :periodId")
    fun findByPeriodIdOrParentPeriodId(@Param("periodId") periodId: Long): List<Task>

    @Modifying
    @Query("update Task t set t.goal = null where t.goal.id = :goalId")
    fun unlinkGoal(@Param("goalId") goalId: Long): Int
}

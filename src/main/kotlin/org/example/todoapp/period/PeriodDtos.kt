package org.example.todoapp.period

import jakarta.validation.constraints.Size
import org.example.todoapp.goal.GoalDto
import java.time.LocalDate

data class PlanPeriodDto(
    val id: Long,
    val type: PeriodType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val note: String?,
    val parentId: Long?,
)

data class MonthViewDto(
    val period: PlanPeriodDto,
    val goals: List<GoalDto>,
    val weeks: List<WeekSummaryDto>,
)

data class WeekSummaryDto(
    val periodId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isoWeekNumber: Int,
    val tasksDone: Int,
    val tasksTotal: Int,
)

data class NoteRequest(
    @field:Size(max = 20_000)
    val note: String?,
)

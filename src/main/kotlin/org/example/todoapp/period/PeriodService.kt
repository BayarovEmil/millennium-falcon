package org.example.todoapp.period

import org.example.todoapp.common.DomainRuleException
import org.example.todoapp.common.NotFoundException
import org.example.todoapp.goal.GoalService
import org.example.todoapp.task.TaskRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields

@Service
class PeriodService(
    private val periodRepository: PlanPeriodRepository,
    private val taskRepository: TaskRepository,
    private val goalService: GoalService,
) {

    @Transactional
    fun getOrCreateDay(date: LocalDate): PlanPeriodDto = getOrCreateDayEntity(date).toDto()

    @Transactional
    fun getOrCreateWeek(anyDateInWeek: LocalDate): PlanPeriodDto = getOrCreateWeekEntity(anyDateInWeek).toDto()

    @Transactional
    fun getOrCreateMonth(yearMonth: YearMonth): MonthViewDto {
        val monthEntity = getOrCreateMonthEntity(yearMonth)
        val goals = goalService.list(periodId = monthEntity.id, status = null, type = null)
        val weeks = weeksOwnedBy(yearMonth).map { buildWeekSummary(it) }
        return MonthViewDto(period = monthEntity.toDto(), goals = goals, weeks = weeks)
    }

    @Transactional
    fun updateNote(id: Long, request: NoteRequest): PlanPeriodDto {
        val period = periodRepository.findById(id).orElseThrow { NotFoundException("Period $id not found") }
        period.note = request.note?.takeIf { it.isNotBlank() }
        return period.toDto()
    }

    private fun getOrCreateDayEntity(date: LocalDate): PlanPeriod {
        val week = getOrCreateWeekEntity(date)
        return getOrCreateEntity(PeriodType.DAY, date, date, week)
    }

    private fun getOrCreateWeekEntity(anyDateInWeek: LocalDate): PlanPeriod {
        val (weekStart, weekEnd) = PeriodDates.weekRange(anyDateInWeek)
        val month = getOrCreateMonthEntity(PeriodDates.monthOfWeek(anyDateInWeek))
        return getOrCreateEntity(PeriodType.WEEK, weekStart, weekEnd, month)
    }

    private fun getOrCreateMonthEntity(yearMonth: YearMonth): PlanPeriod {
        val (start, end) = PeriodDates.monthRange(yearMonth)
        return getOrCreateEntity(PeriodType.MONTH, start, end, null)
    }

    /** Idempotent, concurrency-safe get-or-create: on a unique-constraint race, re-fetch instead of failing. */
    private fun getOrCreateEntity(type: PeriodType, start: LocalDate, end: LocalDate, parent: PlanPeriod?): PlanPeriod {
        periodRepository.findByTypeAndStartDate(type, start)?.let { return it }
        requireValidParent(type, parent)
        return try {
            periodRepository.saveAndFlush(PlanPeriod(type = type, startDate = start, endDate = end, parent = parent))
        } catch (ex: DataIntegrityViolationException) {
            periodRepository.findByTypeAndStartDate(type, start) ?: throw ex
        }
    }

    private fun requireValidParent(type: PeriodType, parent: PlanPeriod?) {
        val expectedParentType = when (type) {
            PeriodType.DAY -> PeriodType.WEEK
            PeriodType.WEEK -> PeriodType.MONTH
            PeriodType.MONTH -> null
        }
        if (expectedParentType == null) {
            if (parent != null) throw DomainRuleException("A MONTH period must not have a parent")
        } else if (parent?.type != expectedParentType) {
            throw DomainRuleException("A $type period's parent must be a $expectedParentType")
        }
    }

    private fun weeksOwnedBy(yearMonth: YearMonth): List<LocalDate> {
        val (monthStart, monthEnd) = PeriodDates.monthRange(yearMonth)
        val firstMonday = PeriodDates.weekStart(monthStart.minusDays(6))
        val lastMonday = PeriodDates.weekStart(monthEnd)
        return generateSequence(firstMonday) { it.plusWeeks(1) }
            .takeWhile { it <= lastMonday }
            .filter { PeriodDates.monthOfWeek(it) == yearMonth }
            .toList()
    }

    private fun buildWeekSummary(weekStart: LocalDate): WeekSummaryDto {
        val week = getOrCreateWeekEntity(weekStart)
        val weekId = requireNotNull(week.id)
        val tasks = taskRepository.findByPeriodIdOrParentPeriodId(weekId)
        return WeekSummaryDto(
            periodId = weekId,
            startDate = week.startDate,
            endDate = week.endDate,
            isoWeekNumber = weekStart.get(WeekFields.ISO.weekOfWeekBasedYear()),
            tasksDone = tasks.count { it.done },
            tasksTotal = tasks.size,
        )
    }
}

package org.example.todoapp.task

import org.example.todoapp.common.ConflictException
import org.example.todoapp.common.NotFoundException
import org.example.todoapp.goal.GoalRepository
import org.example.todoapp.goal.GoalStatus
import org.example.todoapp.goal.GoalType
import org.example.todoapp.period.PeriodType
import org.example.todoapp.period.PlanPeriodRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val periodRepository: PlanPeriodRepository,
    private val goalRepository: GoalRepository,
) {

    @Transactional(readOnly = true)
    fun listByPeriod(periodId: Long): List<TaskDto> =
        taskRepository.findByPeriodIdOrderBySortOrderAsc(periodId).map { it.toDto() }

    @Transactional(readOnly = true)
    fun listByRange(from: LocalDate, to: LocalDate): List<TaskDto> =
        taskRepository.findByPeriod_TypeAndPeriod_StartDateBetween(PeriodType.DAY, from, to).map { it.toDto() }

    @Transactional
    fun create(request: CreateTaskRequest): TaskDto {
        val period = periodRepository.findById(request.periodId)
            .orElseThrow { NotFoundException("Period ${request.periodId} not found") }
        val goal = request.goalId?.let { findGoalOrThrow(it) }
        val nextSortOrder = (taskRepository.findByPeriodId(request.periodId).maxOfOrNull { it.sortOrder } ?: -1) + 1
        val task = Task(title = request.title, period = period, goal = goal, sortOrder = nextSortOrder)
        return taskRepository.save(task).toDto()
    }

    @Transactional
    fun update(id: Long, request: UpdateTaskRequest): TaskDto {
        val task = taskRepository.findById(id).orElseThrow { NotFoundException("Task $id not found") }
        val period = periodRepository.findById(request.periodId)
            .orElseThrow { NotFoundException("Period ${request.periodId} not found") }
        task.title = request.title
        task.period = period
        task.goal = request.goalId?.let { findGoalOrThrow(it) }
        return task.toDto()
    }

    @Transactional
    fun delete(id: Long) {
        val task = taskRepository.findById(id).orElseThrow { NotFoundException("Task $id not found") }
        taskRepository.delete(task)
    }

    /** Toggles done state; if linked to a MILESTONE goal, moves currentCount under a pessimistic lock. */
    @Transactional
    fun toggle(id: Long): TaskDto {
        val task = taskRepository.findById(id).orElseThrow { NotFoundException("Task $id not found") }
        val goalId = task.goal?.id
        val goalType = task.goal?.type
        if (task.done) {
            task.done = false
            task.completedAt = null
            if (goalId != null && goalType == GoalType.MILESTONE) applyMilestoneDelta(goalId, -1)
        } else {
            task.done = true
            task.completedAt = Instant.now()
            if (goalId != null && goalType == GoalType.MILESTONE) applyMilestoneDelta(goalId, +1)
        }
        return task.toDto()
    }

    @Transactional
    fun reorder(request: ReorderRequest) {
        val tasksInPeriod = taskRepository.findByPeriodId(request.periodId)
        val periodTaskIds = tasksInPeriod.mapNotNull { it.id }.toSet()
        val requestedIds = request.orderedIds.toSet()
        if (periodTaskIds != requestedIds) {
            throw ConflictException("orderedIds must exactly match the current tasks in period ${request.periodId}")
        }
        val tasksById = tasksInPeriod.associateBy { it.id }
        request.orderedIds.forEachIndexed { index, taskId ->
            tasksById.getValue(taskId).sortOrder = index
        }
    }

    private fun findGoalOrThrow(goalId: Long) =
        goalRepository.findById(goalId).orElseThrow { NotFoundException("Goal $goalId not found") }

    private fun applyMilestoneDelta(goalId: Long, delta: Int) {
        val goal = goalRepository.lockById(goalId) ?: return
        val target = goal.targetCount ?: return
        val newCount = ((goal.currentCount ?: 0) + delta).coerceIn(0, target)
        goal.currentCount = newCount
        goal.status = if (newCount == target) GoalStatus.DONE else GoalStatus.ACTIVE
    }
}

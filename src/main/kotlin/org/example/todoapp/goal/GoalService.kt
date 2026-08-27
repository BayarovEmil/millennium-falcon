package org.example.todoapp.goal

import org.example.todoapp.common.DomainRuleException
import org.example.todoapp.common.NotFoundException
import org.example.todoapp.lifearea.LifeAreaRepository
import org.example.todoapp.period.PlanPeriodRepository
import org.example.todoapp.task.TaskRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GoalService(
    private val goalRepository: GoalRepository,
    private val lifeAreaRepository: LifeAreaRepository,
    private val periodRepository: PlanPeriodRepository,
    private val taskRepository: TaskRepository,
) {

    @Transactional(readOnly = true)
    fun list(periodId: Long?, status: GoalStatus?, type: GoalType?): List<GoalDto> {
        val goals = if (periodId != null) goalRepository.findByPeriodId(periodId) else goalRepository.findAll()
        return goals
            .filter { status == null || it.status == status }
            .filter { type == null || it.type == type }
            .map { it.toDto() }
    }

    @Transactional
    fun create(request: CreateGoalRequest): GoalDto {
        validateCadenceFields(request.type, request.targetCount, request.targetPerWeek)
        val lifeArea = lifeAreaRepository.findById(request.lifeAreaId)
            .orElseThrow { NotFoundException("Life area ${request.lifeAreaId} not found") }
        val period = periodRepository.findById(request.periodId)
            .orElseThrow { NotFoundException("Period ${request.periodId} not found") }
        return goalRepository.save(request.toEntity(lifeArea, period)).toDto()
    }

    @Transactional
    fun update(id: Long, request: UpdateGoalRequest): GoalDto {
        val goal = goalRepository.findById(id).orElseThrow { NotFoundException("Goal $id not found") }
        validateCadenceFields(goal.type, request.targetCount, request.targetPerWeek)
        val lifeArea = lifeAreaRepository.findById(request.lifeAreaId)
            .orElseThrow { NotFoundException("Life area ${request.lifeAreaId} not found") }
        goal.title = request.title
        goal.description = request.description
        goal.lifeArea = lifeArea
        goal.status = request.status
        goal.targetCount = request.targetCount
        goal.targetPerWeek = request.targetPerWeek
        return goal.toDto()
    }

    @Transactional
    fun delete(id: Long) {
        val goal = goalRepository.findById(id).orElseThrow { NotFoundException("Goal $id not found") }
        taskRepository.unlinkGoal(id)
        goalRepository.delete(goal)
    }

    /** MILESTONE goals may carry targetCount but not targetPerWeek, and vice versa for HABIT. */
    private fun validateCadenceFields(type: GoalType, targetCount: Int?, targetPerWeek: Int?) {
        when (type) {
            GoalType.MILESTONE -> if (targetPerWeek != null) {
                throw DomainRuleException("A MILESTONE goal must not set targetPerWeek")
            }
            GoalType.HABIT -> if (targetCount != null) {
                throw DomainRuleException("A HABIT goal must not set targetCount")
            }
        }
    }
}

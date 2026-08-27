package org.example.todoapp.goal

import org.example.todoapp.lifearea.LifeArea
import org.example.todoapp.lifearea.toDto
import org.example.todoapp.period.PlanPeriod

fun Goal.toDto(): GoalDto = GoalDto(
    id = requireNotNull(id),
    title = title,
    description = description,
    type = type,
    status = status,
    lifeArea = lifeArea.toDto(),
    periodId = requireNotNull(period.id),
    targetCount = targetCount,
    currentCount = currentCount,
    targetPerWeek = targetPerWeek,
)

fun CreateGoalRequest.toEntity(lifeArea: LifeArea, period: PlanPeriod): Goal = Goal(
    title = title,
    description = description,
    type = type,
    lifeArea = lifeArea,
    period = period,
    targetCount = targetCount,
    currentCount = if (type == GoalType.MILESTONE) 0 else null,
    targetPerWeek = targetPerWeek,
)

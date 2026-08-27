package org.example.todoapp.task

fun Task.toDto(): TaskDto = TaskDto(
    id = requireNotNull(id),
    title = title,
    periodId = requireNotNull(period.id),
    periodDate = period.startDate,
    goalId = goal?.id,
    lifeAreaId = goal?.lifeArea?.id,
    done = done,
    completedAt = completedAt,
    sortOrder = sortOrder,
)

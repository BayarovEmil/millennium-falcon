package org.example.todoapp.period

fun PlanPeriod.toDto(): PlanPeriodDto = PlanPeriodDto(
    id = requireNotNull(id),
    type = type,
    startDate = startDate,
    endDate = endDate,
    note = note,
    parentId = parent?.id,
)

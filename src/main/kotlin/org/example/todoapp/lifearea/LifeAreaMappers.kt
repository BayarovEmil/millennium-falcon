package org.example.todoapp.lifearea

fun LifeArea.toDto(): LifeAreaDto = LifeAreaDto(
    id = requireNotNull(id),
    name = name,
    color = color,
    sortOrder = sortOrder,
)

fun LifeAreaRequest.toEntity(): LifeArea = LifeArea(
    name = name,
    color = color,
    sortOrder = sortOrder,
)

package org.example.todoapp.task

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.Instant
import java.time.LocalDate

data class TaskDto(
    val id: Long,
    val title: String,
    val periodId: Long,
    val periodDate: LocalDate,
    val goalId: Long?,
    val lifeAreaId: Long?,
    val done: Boolean,
    val completedAt: Instant?,
    val sortOrder: Int,
)

data class CreateTaskRequest(
    @field:NotBlank
    @field:Size(max = 300)
    val title: String,
    @field:Positive
    val periodId: Long,
    @field:Positive
    val goalId: Long? = null,
)

data class UpdateTaskRequest(
    @field:NotBlank
    @field:Size(max = 300)
    val title: String,
    @field:Positive
    val goalId: Long? = null,
    @field:Positive
    val periodId: Long,
)

data class ReorderRequest(
    @field:Positive
    val periodId: Long,
    @field:NotEmpty
    val orderedIds: List<Long>,
)

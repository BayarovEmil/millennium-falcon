package org.example.todoapp.goal

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import org.example.todoapp.lifearea.LifeAreaDto

data class GoalDto(
    val id: Long,
    val title: String,
    val description: String?,
    val type: GoalType,
    val status: GoalStatus,
    val lifeArea: LifeAreaDto,
    val periodId: Long,
    val targetCount: Int?,
    val currentCount: Int?,
    val targetPerWeek: Int?,
)

data class CreateGoalRequest(
    @field:NotBlank
    @field:Size(max = 200)
    val title: String,
    @field:Size(max = 2000)
    val description: String? = null,
    val type: GoalType,
    @field:Positive
    val lifeAreaId: Long,
    @field:Positive
    val periodId: Long,
    @field:Positive
    val targetCount: Int? = null,
    @field:Positive
    @field:Max(7)
    val targetPerWeek: Int? = null,
)

data class UpdateGoalRequest(
    @field:NotBlank
    @field:Size(max = 200)
    val title: String,
    @field:Size(max = 2000)
    val description: String? = null,
    @field:Positive
    val lifeAreaId: Long,
    val status: GoalStatus,
    @field:Positive
    val targetCount: Int? = null,
    @field:Positive
    @field:Max(7)
    val targetPerWeek: Int? = null,
)

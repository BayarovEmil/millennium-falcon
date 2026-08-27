package org.example.todoapp.lifearea

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class LifeAreaDto(
    val id: Long,
    val name: String,
    val color: String,
    val sortOrder: Int,
)

data class LifeAreaRequest(
    @field:NotBlank
    @field:Size(max = 60)
    val name: String,
    @field:Pattern(regexp = "^#[0-9a-fA-F]{6}$")
    val color: String,
    val sortOrder: Int = 0,
)

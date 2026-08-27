package org.example.todoapp.goal

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/goals")
class GoalController(private val goalService: GoalService) {

    @GetMapping
    fun list(
        @RequestParam(required = false) periodId: Long?,
        @RequestParam(required = false) status: GoalStatus?,
        @RequestParam(required = false) type: GoalType?,
    ): List<GoalDto> = goalService.list(periodId, status, type)

    @PostMapping
    fun create(@Valid @RequestBody request: CreateGoalRequest): ResponseEntity<GoalDto> {
        val created = goalService.create(request)
        return ResponseEntity.created(URI.create("/api/goals/${created.id}")).body(created)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: UpdateGoalRequest): GoalDto =
        goalService.update(id, request)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        goalService.delete(id)
        return ResponseEntity.noContent().build()
    }
}

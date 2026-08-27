package org.example.todoapp.task

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private const val MAX_RANGE_DAYS = 366L

@RestController
@RequestMapping("/api/tasks")
class TaskController(private val taskService: TaskService) {

    @GetMapping
    fun list(
        @RequestParam(required = false) periodId: Long?,
        @RequestParam(required = false) from: LocalDate?,
        @RequestParam(required = false) to: LocalDate?,
    ): List<TaskDto> {
        val hasPeriodId = periodId != null
        val hasRange = from != null && to != null
        if (hasPeriodId == hasRange) {
            throw IllegalArgumentException("Provide exactly one of periodId or a from/to date range")
        }
        if (hasRange) {
            val days = ChronoUnit.DAYS.between(from, to) + 1
            if (days < 1 || days > MAX_RANGE_DAYS) {
                throw IllegalArgumentException("from/to range must be non-negative and at most $MAX_RANGE_DAYS days")
            }
            return taskService.listByRange(from!!, to!!)
        }
        return taskService.listByPeriod(periodId!!)
    }

    @PostMapping
    fun create(@Valid @RequestBody request: CreateTaskRequest): ResponseEntity<TaskDto> {
        val created = taskService.create(request)
        return ResponseEntity.created(URI.create("/api/tasks/${created.id}")).body(created)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: UpdateTaskRequest): TaskDto =
        taskService.update(id, request)

    @PatchMapping("/{id}/toggle")
    fun toggle(@PathVariable id: Long): TaskDto = taskService.toggle(id)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        taskService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/reorder")
    fun reorder(@Valid @RequestBody request: ReorderRequest): ResponseEntity<Void> {
        taskService.reorder(request)
        return ResponseEntity.noContent().build()
    }
}

package org.example.todoapp.period

import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.YearMonth

@RestController
@RequestMapping("/api/periods")
class PeriodController(private val periodService: PeriodService) {

    @GetMapping("/month/{yearMonth}")
    fun getMonth(@PathVariable @DateTimeFormat(pattern = "yyyy-MM") yearMonth: YearMonth): MonthViewDto =
        periodService.getOrCreateMonth(yearMonth)

    @GetMapping("/week/{date}")
    fun getWeek(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate): PlanPeriodDto =
        periodService.getOrCreateWeek(date)

    @GetMapping("/day/{date}")
    fun getDay(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate): PlanPeriodDto =
        periodService.getOrCreateDay(date)

    @PutMapping("/{id}/note")
    fun updateNote(@PathVariable id: Long, @Valid @RequestBody request: NoteRequest): PlanPeriodDto =
        periodService.updateNote(id, request)
}

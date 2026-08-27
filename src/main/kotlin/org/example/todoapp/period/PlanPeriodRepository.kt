package org.example.todoapp.period

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface PlanPeriodRepository : JpaRepository<PlanPeriod, Long> {
    fun findByTypeAndStartDate(type: PeriodType, startDate: LocalDate): PlanPeriod?
}

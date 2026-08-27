package org.example.todoapp.period

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.example.todoapp.common.BaseEntity
import java.time.LocalDate

@Entity
@Table(
    name = "plan_periods",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_plan_periods_type_start", columnNames = ["type", "start_date"]),
    ],
)
class PlanPeriod(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var type: PeriodType,
    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate,
    @Column(name = "end_date", nullable = false)
    var endDate: LocalDate,
    @Column(columnDefinition = "TEXT")
    var note: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: PlanPeriod? = null,
) : BaseEntity()

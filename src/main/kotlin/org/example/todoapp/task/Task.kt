package org.example.todoapp.task

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.example.todoapp.common.BaseEntity
import org.example.todoapp.goal.Goal
import org.example.todoapp.period.PlanPeriod
import java.time.Instant

@Entity
@Table(name = "tasks")
class Task(
    @Column(nullable = false)
    var title: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    var period: PlanPeriod,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    var goal: Goal? = null,
    @Column(nullable = false)
    var done: Boolean = false,
    @Column(name = "completed_at")
    var completedAt: Instant? = null,
    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int = 0,
) : BaseEntity()

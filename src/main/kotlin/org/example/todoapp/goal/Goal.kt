package org.example.todoapp.goal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.example.todoapp.common.BaseEntity
import org.example.todoapp.lifearea.LifeArea
import org.example.todoapp.period.PlanPeriod
import java.time.Instant

@Entity
@Table(name = "goals")
class Goal(
    @Column(nullable = false)
    var title: String,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var type: GoalType,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "life_area_id", nullable = false)
    var lifeArea: LifeArea,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    var period: PlanPeriod,
    @Column(name = "target_count")
    var targetCount: Int? = null,
    @Column(name = "current_count")
    var currentCount: Int? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var status: GoalStatus = GoalStatus.ACTIVE,
    @Column(name = "target_per_week")
    var targetPerWeek: Int? = null,
    @Column(name = "archived_at")
    var archivedAt: Instant? = null,
) : BaseEntity()

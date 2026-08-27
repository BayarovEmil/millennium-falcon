package org.example.todoapp.lifearea

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.example.todoapp.common.BaseEntity

@Entity
@Table(name = "life_areas")
class LifeArea(
    @Column(nullable = false, unique = true)
    var name: String,
    @Column(nullable = false)
    var color: String,
    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int = 0,
) : BaseEntity()

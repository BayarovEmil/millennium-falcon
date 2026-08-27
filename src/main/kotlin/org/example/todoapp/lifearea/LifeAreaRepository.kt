package org.example.todoapp.lifearea

import org.springframework.data.jpa.repository.JpaRepository

interface LifeAreaRepository : JpaRepository<LifeArea, Long> {
    fun findByNameIgnoreCase(name: String): LifeArea?

    fun existsByNameIgnoreCase(name: String): Boolean
}

package org.example.todoapp.goal

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface GoalRepository : JpaRepository<Goal, Long> {
    fun findByPeriodId(periodId: Long): List<Goal>

    fun existsByLifeAreaId(lifeAreaId: Long): Boolean

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from Goal g where g.id = :id")
    fun lockById(@Param("id") id: Long): Goal?

    @Modifying
    @Query("update Goal g set g.lifeArea.id = :targetId where g.lifeArea.id = :sourceId")
    fun reassignLifeArea(@Param("sourceId") sourceId: Long, @Param("targetId") targetId: Long): Int
}

package org.example.todoapp.lifearea

import org.example.todoapp.common.ConflictException
import org.example.todoapp.common.NotFoundException
import org.example.todoapp.goal.GoalRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LifeAreaService(
    private val lifeAreaRepository: LifeAreaRepository,
    private val goalRepository: GoalRepository,
) {

    @Transactional(readOnly = true)
    fun list(): List<LifeAreaDto> =
        lifeAreaRepository.findAll()
            .sortedWith(compareBy(LifeArea::sortOrder, LifeArea::name))
            .map { it.toDto() }

    @Transactional
    fun create(request: LifeAreaRequest): LifeAreaDto {
        requireUniqueName(request.name)
        return lifeAreaRepository.save(request.toEntity()).toDto()
    }

    @Transactional
    fun update(id: Long, request: LifeAreaRequest): LifeAreaDto {
        val area = findOrThrow(id)
        if (!area.name.equals(request.name, ignoreCase = true)) {
            requireUniqueName(request.name)
        }
        area.name = request.name
        area.color = request.color
        area.sortOrder = request.sortOrder
        return area.toDto()
    }

    @Transactional
    fun delete(id: Long, reassignTo: Long?) {
        val area = findOrThrow(id)
        if (reassignTo != null && reassignTo == id) {
            throw IllegalArgumentException("reassignTo must differ from the life area being deleted")
        }
        if (goalRepository.existsByLifeAreaId(id)) {
            val targetId = reassignTo
                ?: throw ConflictException("Life area $id has goals attached; pass reassignTo to move them first")
            if (!lifeAreaRepository.existsById(targetId)) {
                throw NotFoundException("Life area $targetId not found")
            }
            goalRepository.reassignLifeArea(id, targetId)
        }
        lifeAreaRepository.delete(area)
    }

    private fun findOrThrow(id: Long): LifeArea =
        lifeAreaRepository.findById(id).orElseThrow { NotFoundException("Life area $id not found") }

    private fun requireUniqueName(name: String) {
        if (lifeAreaRepository.existsByNameIgnoreCase(name)) {
            throw ConflictException("A life area named '$name' already exists")
        }
    }
}

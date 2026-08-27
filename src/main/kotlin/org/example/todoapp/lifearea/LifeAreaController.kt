package org.example.todoapp.lifearea

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/life-areas")
class LifeAreaController(private val lifeAreaService: LifeAreaService) {

    @GetMapping
    fun list(): List<LifeAreaDto> = lifeAreaService.list()

    @PostMapping
    fun create(@Valid @RequestBody request: LifeAreaRequest): ResponseEntity<LifeAreaDto> {
        val created = lifeAreaService.create(request)
        return ResponseEntity.created(URI.create("/api/life-areas/${created.id}")).body(created)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: LifeAreaRequest): LifeAreaDto =
        lifeAreaService.update(id, request)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, @RequestParam(required = false) reassignTo: Long?): ResponseEntity<Void> {
        lifeAreaService.delete(id, reassignTo)
        return ResponseEntity.noContent().build()
    }
}

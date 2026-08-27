package org.example.todoapp.lifearea

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.todoapp.common.IntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

class LifeAreaControllerTest : IntegrationTest() {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `GET life-areas returns the four seeded areas sorted by sortOrder`() {
        mockMvc.get("/api/life-areas").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(4) }
            jsonPath("$[0].name") { value("English") }
            jsonPath("$[3].name") { value("Health") }
        }
    }

    @Test
    fun `POST creates a life area and returns 201 with a Location header`() {
        mockMvc.post("/api/life-areas") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"Finance","color":"#112233","sortOrder":4}"""
        }.andExpect {
            status { isCreated() }
            header { exists("Location") }
            jsonPath("$.name") { value("Finance") }
        }
    }

    @Test
    fun `POST with a duplicate name returns 409`() {
        mockMvc.post("/api/life-areas") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"English","color":"#112233","sortOrder":9}"""
        }.andExpect {
            status { isConflict() }
            content { contentType(MediaType.valueOf("application/problem+json")) }
        }
    }

    @Test
    fun `DELETE with goals attached and no reassignTo returns 409`() {
        val areaId = 1L
        createGoalInArea(areaId)

        mockMvc.delete("/api/life-areas/$areaId").andExpect {
            status { isConflict() }
        }
    }

    @Test
    fun `DELETE with reassignTo moves the goals and returns 204`() {
        val sourceId = 1L
        val targetId = 2L
        val goalId = createGoalInArea(sourceId)

        mockMvc.delete("/api/life-areas/$sourceId?reassignTo=$targetId").andExpect {
            status { isNoContent() }
        }

        mockMvc.get("/api/goals").andExpect {
            status { isOk() }
            jsonPath("$[?(@.id == $goalId)].lifeArea.id") { value(targetId.toInt()) }
        }
    }

    @Test
    fun `DELETE with reassignTo equal to its own id returns 400`() {
        mockMvc.delete("/api/life-areas/1?reassignTo=1").andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `PUT updates a life area`() {
        mockMvc.put("/api/life-areas/1") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"English (renamed)","color":"#f59e0b","sortOrder":0}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("English (renamed)") }
        }
    }

    private fun createGoalInArea(areaId: Long): Long {
        val periodJson = mockMvc.get("/api/periods/month/2026-09").andReturn().response.contentAsString
        val periodId = objectMapper.readTree(periodJson)["period"]["id"].asLong()

        val goalResponse = mockMvc.post("/api/goals") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {"title":"Read a book","type":"MILESTONE","lifeAreaId":$areaId,"periodId":$periodId,"targetCount":3}
            """.trimIndent()
        }.andExpect { status { isCreated() } }.andReturn().response.contentAsString
        return objectMapper.readTree(goalResponse)["id"].asLong()
    }
}

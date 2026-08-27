package org.example.todoapp.task

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.todoapp.common.IntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

class TaskControllerTest : IntegrationTest() {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `toggling a task three times completes a targetCount 3 milestone, then reverts on untoggle`() {
        val dayId = dayPeriodId("2026-09-14")
        val goalId = createMilestoneGoal(targetCount = 3)
        val taskIds = (1..3).map { createTask("Chapter $it", dayId, goalId) }

        taskIds.forEach { toggleTask(it) }

        mockMvc.get("/api/goals?periodId=${monthPeriodId()}").andExpect {
            jsonPath("$[?(@.id == $goalId)].currentCount") { value(3) }
            jsonPath("$[?(@.id == $goalId)].status") { value("DONE") }
        }

        toggleTask(taskIds[0])

        mockMvc.get("/api/goals?periodId=${monthPeriodId()}").andExpect {
            jsonPath("$[?(@.id == $goalId)].currentCount") { value(2) }
            jsonPath("$[?(@.id == $goalId)].status") { value("ACTIVE") }
        }
    }

    @Test
    fun `toggling a task with no goal does not blow up`() {
        val dayId = dayPeriodId("2026-09-15")
        val taskId = createTask("Unlinked task", dayId, goalId = null)

        mockMvc.patch("/api/tasks/$taskId/toggle").andExpect {
            status { isOk() }
            jsonPath("$.done") { value(true) }
        }
    }

    @Test
    fun `reorder with a partial id list returns 409`() {
        val dayId = dayPeriodId("2026-09-16")
        val first = createTask("A", dayId, null)
        createTask("B", dayId, null)

        mockMvc.put("/api/tasks/reorder") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"periodId":$dayId,"orderedIds":[$first]}"""
        }.andExpect {
            status { isConflict() }
        }
    }

    @Test
    fun `GET tasks with neither periodId nor a range returns 400`() {
        mockMvc.get("/api/tasks").andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `GET tasks with both periodId and a range returns 400`() {
        mockMvc.get("/api/tasks?periodId=1&from=2026-09-01&to=2026-09-30").andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST with a blank title returns 400 with a field-level error`() {
        val dayId = dayPeriodId("2026-09-17")

        mockMvc.post("/api/tasks") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":"","periodId":$dayId}"""
        }.andExpect {
            status { isBadRequest() }
            content { contentType(MediaType.valueOf("application/problem+json")) }
            jsonPath("$.errors[?(@.field == 'title')]") { exists() }
        }
    }

    private fun toggleTask(id: Long) {
        mockMvc.patch("/api/tasks/$id/toggle").andExpect { status { isOk() } }
    }

    private fun dayPeriodId(date: String): Long {
        val json = mockMvc.get("/api/periods/day/$date").andReturn().response.contentAsString
        return objectMapper.readTree(json)["id"].asLong()
    }

    private fun monthPeriodId(): Long {
        val json = mockMvc.get("/api/periods/month/2026-09").andReturn().response.contentAsString
        return objectMapper.readTree(json)["period"]["id"].asLong()
    }

    private fun createMilestoneGoal(targetCount: Int): Long {
        val response = mockMvc.post("/api/goals") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {"title":"Finish the book","type":"MILESTONE","lifeAreaId":2,
                 "periodId":${monthPeriodId()},"targetCount":$targetCount}
            """.trimIndent()
        }.andReturn().response.contentAsString
        return objectMapper.readTree(response)["id"].asLong()
    }

    private fun createTask(title: String, periodId: Long, goalId: Long?): Long {
        val goalField = if (goalId != null) ""","goalId":$goalId""" else ""
        val response = mockMvc.post("/api/tasks") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":"$title","periodId":$periodId$goalField}"""
        }.andExpect { status { isCreated() } }.andReturn().response.contentAsString
        return objectMapper.readTree(response)["id"].asLong()
    }
}

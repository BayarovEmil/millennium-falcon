package org.example.todoapp.goal

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.todoapp.common.IntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

class GoalControllerTest : IntegrationTest() {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `creating a HABIT goal with targetCount set returns 409 problem+json`() {
        val periodId = monthPeriodId()

        mockMvc.post("/api/goals") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {"title":"Gym","type":"HABIT","lifeAreaId":4,"periodId":$periodId,
                 "targetCount":5,"targetPerWeek":3}
            """.trimIndent()
        }.andExpect {
            status { isConflict() }
            content { contentType(MediaType.valueOf("application/problem+json")) }
            jsonPath("$.type") { value("/errors/domain-rule") }
        }
    }

    @Test
    fun `creating a MILESTONE goal with targetPerWeek set returns 409`() {
        val periodId = monthPeriodId()

        mockMvc.post("/api/goals") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {"title":"DDIA ch 1-6","type":"MILESTONE","lifeAreaId":2,"periodId":$periodId,
                 "targetCount":6,"targetPerWeek":2}
            """.trimIndent()
        }.andExpect { status { isConflict() } }
    }

    @Test
    fun `a valid MILESTONE goal is created with currentCount starting at 0`() {
        val periodId = monthPeriodId()

        mockMvc.post("/api/goals") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {"title":"100 Codeforces problems","type":"MILESTONE","lifeAreaId":3,
                 "periodId":$periodId,"targetCount":100}
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.currentCount") { value(0) }
            jsonPath("$.status") { value("ACTIVE") }
        }
    }

    private fun monthPeriodId(): Long {
        val json = mockMvc.get("/api/periods/month/2026-09").andReturn().response.contentAsString
        return objectMapper.readTree(json)["period"]["id"].asLong()
    }
}

package org.example.todoapp.period

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.todoapp.common.IntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put

class PeriodControllerTest : IntegrationTest() {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `GET day lazily creates day, week and month, and is idempotent`() {
        val first = mockMvc.get("/api/periods/day/2026-09-14").andExpect {
            status { isOk() }
            jsonPath("$.type") { value("DAY") }
            jsonPath("$.startDate") { value("2026-09-14") }
        }.andReturn().response.contentAsString
        val firstId = objectMapper.readTree(first)["id"].asLong()
        val firstParentId = objectMapper.readTree(first)["parentId"].asLong()

        val second = mockMvc.get("/api/periods/day/2026-09-14").andExpect {
            status { isOk() }
        }.andReturn().response.contentAsString
        val secondId = objectMapper.readTree(second)["id"].asLong()

        assert(firstId == secondId) { "calling twice must return the same day id" }

        val week = mockMvc.get("/api/periods/week/2026-09-14").andReturn().response.contentAsString
        val weekId = objectMapper.readTree(week)["id"].asLong()
        assert(firstParentId == weekId) { "day's parent must be the week" }

        val weekJson = objectMapper.readTree(week)
        assert(weekJson["startDate"].asText() == "2026-09-14")
        assert(weekJson["endDate"].asText() == "2026-09-20")

        val monthParentId = weekJson["parentId"].asLong()
        val month = mockMvc.get("/api/periods/month/2026-09").andReturn().response.contentAsString
        val monthId = objectMapper.readTree(month)["period"]["id"].asLong()
        assert(monthParentId == monthId) { "week's parent must be the month" }
    }

    @Test
    fun `GET week for a year-boundary date lands in January of the following year`() {
        // Mon 2025-12-29 .. Sun 2026-01-04, Thursday is 2026-01-01, so it belongs to January 2026.
        mockMvc.get("/api/periods/week/2025-12-30").andExpect {
            status { isOk() }
            jsonPath("$.startDate") { value("2025-12-29") }
            jsonPath("$.endDate") { value("2026-01-04") }
        }

        mockMvc.get("/api/periods/month/2026-01").andExpect {
            status { isOk() }
            jsonPath("$.weeks[?(@.startDate == '2025-12-29')]") { exists() }
        }
    }

    @Test
    fun `GET month on an unparseable value returns 400`() {
        mockMvc.get("/api/periods/month/not-a-month").andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `PUT note sets and clears the period note`() {
        val dayJson = mockMvc.get("/api/periods/day/2026-09-14").andReturn().response.contentAsString
        val dayId = objectMapper.readTree(dayJson)["id"].asLong()

        mockMvc.put("/api/periods/$dayId/note") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"note":"Focus on deep work."}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.note") { value("Focus on deep work.") }
        }

        mockMvc.put("/api/periods/$dayId/note") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"note":"   "}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.note") { doesNotExist() }
        }
    }
}

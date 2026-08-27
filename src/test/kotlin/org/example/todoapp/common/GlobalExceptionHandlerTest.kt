package org.example.todoapp.common

import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Throwaway proof that the error-handling wiring actually works end to end,
 * via a test-only controller registered just for this test class.
 */
@Import(GlobalExceptionHandlerTest.ThrowingTestController::class)
class GlobalExceptionHandlerTest : IntegrationTest() {

    @Test
    fun `NotFoundException renders as an RFC 7807 problem+json response`() {
        mockMvc.get("/api/_test/boom").andExpect {
            status { isNotFound() }
            content { contentType(MediaType.valueOf("application/problem+json")) }
            jsonPath("$.status") { value(404) }
            jsonPath("$.type") { value("/errors/not-found") }
            jsonPath("$.detail") { value("widget 42 not found") }
        }
    }

    @RestController
    class ThrowingTestController {
        @GetMapping("/api/_test/boom")
        fun boom(): Nothing = throw NotFoundException("widget 42 not found")
    }
}

package org.example.todoapp.common

import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.PostgreSQLContainer

/**
 * Base class for API integration tests. Boots the full Spring context against a single
 * Postgres container shared across every subclass (started once, never stopped - Testcontainers'
 * Ryuk reaper tears it down when the JVM exits). Tables are truncated and life areas reseeded
 * from the real V2 migration after each test, rather than relying on @Transactional rollback,
 * so commit-time constraint failures (uniqueness, FKs) still surface.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
abstract class IntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @AfterEach
    fun cleanDatabase() {
        jdbcTemplate.execute(
            "TRUNCATE TABLE habit_entries, tasks, goals, plan_periods, life_areas RESTART IDENTITY CASCADE",
        )
        jdbcTemplate.execute(seedLifeAreasSql)
    }

    companion object {
        private val postgres: PostgreSQLContainer<*> =
            PostgreSQLContainer("postgres:16")
                .withDatabaseName("planner_test")
                .withUsername("planner")
                .withPassword("planner")
                .also { it.start() }

        private val seedLifeAreasSql: String =
            ClassPathResource("db/migration/V2__seed_life_areas.sql").inputStream
                .bufferedReader()
                .readText()

        @JvmStatic
        @DynamicPropertySource
        fun registerDatasourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}

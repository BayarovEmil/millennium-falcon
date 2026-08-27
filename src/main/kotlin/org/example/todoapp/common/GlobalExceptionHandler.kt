package org.example.todoapp.common

import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.net.URI
import java.util.UUID

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ProblemDetail =
        problem(HttpStatus.NOT_FOUND, "/errors/not-found", ex.message ?: "Not found")

    @ExceptionHandler(DomainRuleException::class)
    fun handleDomainRule(ex: DomainRuleException): ProblemDetail =
        problem(HttpStatus.CONFLICT, "/errors/domain-rule", ex.message ?: "Domain rule violated")

    @ExceptionHandler(ConflictException::class, DataIntegrityViolationException::class)
    fun handleConflict(ex: Exception): ProblemDetail =
        problem(HttpStatus.CONFLICT, "/errors/conflict", ex.message ?: "Conflict")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ProblemDetail {
        val problemDetail = problem(HttpStatus.BAD_REQUEST, "/errors/validation", "Validation failed")
        problemDetail.setProperty(
            "errors",
            ex.bindingResult.fieldErrors.map {
                mapOf("field" to it.field, "message" to (it.defaultMessage ?: "invalid"))
            },
        )
        return problemDetail
    }

    @ExceptionHandler(
        MethodArgumentTypeMismatchException::class,
        HttpMessageNotReadableException::class,
        ConstraintViolationException::class,
        IllegalArgumentException::class,
    )
    fun handleBadRequest(ex: Exception): ProblemDetail =
        problem(HttpStatus.BAD_REQUEST, "/errors/bad-request", ex.message ?: "Bad request")

    @ExceptionHandler(Exception::class)
    fun handleInternal(ex: Exception): ProblemDetail {
        val traceId = UUID.randomUUID().toString()
        log.error("Unhandled exception, traceId={}", traceId, ex)
        val problemDetail = problem(HttpStatus.INTERNAL_SERVER_ERROR, "/errors/internal", "Internal server error")
        problemDetail.setProperty("traceId", traceId)
        return problemDetail
    }

    private fun problem(status: HttpStatus, typeSlug: String, detail: String): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(status, detail)
        problemDetail.type = URI.create(typeSlug)
        return problemDetail
    }
}

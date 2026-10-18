package com.devlink.backend.exception

import com.devlink.backend.dto.ApiError
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(
        EmailAlreadyExistsException::class,
        UsernameAlreadyExistsException::class
    )
    fun handleConflict(ex: RuntimeException, req: HttpServletRequest): ResponseEntity<ApiError> =
        build(HttpStatus.CONFLICT, ex.message, req)

    @ExceptionHandler(
        InvalidCredentialsException::class,
        BadCredentialsException::class,
        OtpInvalidException::class
    )
    fun handleUnauthorized(ex: RuntimeException, req: HttpServletRequest): ResponseEntity<ApiError> =
        build(HttpStatus.UNAUTHORIZED, ex.message, req)

    @ExceptionHandler(
        UserDisabledException::class,
        OtpMaxAttemptsExceededException::class
    )
    fun handleForbidden(ex: RuntimeException, req: HttpServletRequest): ResponseEntity<ApiError> =
        build(HttpStatus.FORBIDDEN, ex.message, req)

    @ExceptionHandler(
        OtpNotFoundException::class,
        OtpExpiredException::class,
        RefreshTokenInvalidException::class
    )
    fun handleBadRequest(ex: RuntimeException, req: HttpServletRequest): ResponseEntity<ApiError> =
        build(HttpStatus.BAD_REQUEST, ex.message, req)

    @ExceptionHandler(
        ProfileNotFoundException::class,
        ProfileNotFoundByUsernameException::class
    )
    fun handleNotFound(ex: RuntimeException, req: HttpServletRequest): ResponseEntity<ApiError> =
        build(HttpStatus.NOT_FOUND, ex.message, req)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException, req: HttpServletRequest): ResponseEntity<ApiError> =
        build(HttpStatus.BAD_REQUEST, ex.message, req)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException, req: HttpServletRequest): ResponseEntity<ApiError> {
        val message = ex.bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return build(HttpStatus.BAD_REQUEST, message, req)
    }

    @ExceptionHandler(Exception::class)
    fun handleAllUncaughtException(ex: Exception, req: HttpServletRequest): ResponseEntity<ApiError> {
        log.error("Unhandled error processing request to {}: {}", req.requestURI, ex.message, ex)
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.message ?: "An unexpected internal server error occurred", req)
    }

    private fun build(status: HttpStatus, message: String?, req: HttpServletRequest): ResponseEntity<ApiError> =
        ResponseEntity.status(status).body(
            ApiError(
                status = status.value(),
                error = status.reasonPhrase,
                message = message,
                path = req.requestURI
            )
        )
}

package org.minh.template.exception;

import io.jsonwebtoken.MalformedJwtException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.coyote.BadRequestException;
import org.minh.template.dto.response.error.DetailedErrorResponse;
import org.minh.template.dto.response.error.ErrorResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.HashMap;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class AppExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public final ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(
            final HttpRequestMethodNotSupportedException e) {
        log.error(e.toString(), e.getMessage());
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed: " + e.getMethod());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public final ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(final HttpMessageNotReadableException e) {
        log.error(e.toString(), e.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON request: " + e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public final ResponseEntity<ErrorResponse> handleBindException(final BindException e) {
        log.error(e.toString(), e.getMessage());
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        return build(HttpStatus.UNPROCESSABLE_ENTITY, "Validation error", errors);
    }

    @ExceptionHandler({
            BadRequestException.class,
            MultipartException.class,
            MissingServletRequestPartException.class,
            HttpMediaTypeNotSupportedException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class,
            InvalidDataAccessApiUsageException.class,
            ConstraintViolationException.class,
            MissingRequestHeaderException.class,
            MalformedJwtException.class
    })
    public final ResponseEntity<ErrorResponse> handleBadRequestException(final Exception e) {
        log.error(e.toString(), e.getMessage());
        return build(HttpStatus.BAD_REQUEST, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
    }

    @ExceptionHandler(ChangeSetPersister.NotFoundException.class)
    public final ResponseEntity<ErrorResponse> handleNotFoundException(final ChangeSetPersister.NotFoundException e) {
        log.error(e.toString(), e.getMessage());
        return build(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({
            InternalAuthenticationServiceException.class,
            BadCredentialsException.class,
            AuthenticationCredentialsNotFoundException.class
    })
    public final ResponseEntity<ErrorResponse> handleBadCredentialsException(final Exception e) {
        log.error(e.toString(), e.getMessage());
        return build(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public final ResponseEntity<ErrorResponse> handleAccessDeniedException(final Exception e) {
        log.error(e.toString(), e.getMessage());
        return build(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorResponse> handleAllExceptions(final Exception e) {
        log.error("Exception: {}", ExceptionUtils.getStackTrace(e));
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Server cooked!!!");
    }

    /**
     * Build error response.
     *
     * @param httpStatus HttpStatus enum to response status field
     * @param message    String for response message field
     * @param errors     Map for response errors field
     * @return ResponseEntity
     */
    private ResponseEntity<ErrorResponse> build(final HttpStatus httpStatus,
                                                final String message,
                                                final Map<String, String> errors) {
        if (!errors.isEmpty()) {
            return ResponseEntity.status(httpStatus).body(
                    DetailedErrorResponse.builder()
                        .message(message)
                        .statusCode(httpStatus.value())
                        .items(errors)
                        .build());
        }

        return ResponseEntity.status(httpStatus).body(ErrorResponse.builder()
                .message(message)
                .statusCode(httpStatus.value())
                .build());
    }

    /**
     * Build error response.
     *
     * @param httpStatus HttpStatus enum to response status field
     * @param message    String for response message field
     * @return ResponseEntity
     */
    private ResponseEntity<ErrorResponse> build(final HttpStatus httpStatus, final String message) {
        return build(httpStatus, message, new HashMap<>());
    }
}

package za.co.sheeptracker.common;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {
    public record ApiError(OffsetDateTime timestamp, int status, String error, String message, String path) {
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> dataIntegrity(
            DataIntegrityViolationException e,
            HttpServletRequest request) {

        log.error(
                "Database operation failed: method={}, path={}",
                request.getMethod(),
                request.getRequestURI(),
                e
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ApiError(
                        OffsetDateTime.now(ZoneOffset.UTC),
                        409,
                        "CONFLICT",
                        "The request violates a database constraint.",
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<ApiError> missing(NoSuchElementException e, jakarta.servlet.http.HttpServletRequest r) {
        return ResponseEntity.status(404).body(new ApiError(OffsetDateTime.now(ZoneOffset.UTC), 404, "NOT_FOUND", e.getMessage(), r.getRequestURI()));
    }

    @ExceptionHandler({IllegalArgumentException.class})
    ResponseEntity<ApiError> bad(IllegalArgumentException e, jakarta.servlet.http.HttpServletRequest r) {
        return ResponseEntity.badRequest().body(new ApiError(OffsetDateTime.now(ZoneOffset.UTC), 400, "BAD_REQUEST", e.getMessage(), r.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> invalid(MethodArgumentNotValidException e, jakarta.servlet.http.HttpServletRequest r) {
        Map<String, String> fields = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(x -> fields.put(x.getField(), x.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString(), "status", 400, "error", "VALIDATION_FAILED", "fields", fields, "path", r.getRequestURI()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> databaseError(
            DataAccessException e,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiError(
                        OffsetDateTime.now(ZoneOffset.UTC),
                        500,
                        "DATABASE_ERROR",
                        "A database error occurred while processing the request.",
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(
            Exception e,
            HttpServletRequest request) {

        log.error(
                "Unhandled exception: method={}, path={}",
                request.getMethod(),
                request.getRequestURI(),
                e
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(
                        OffsetDateTime.now(ZoneOffset.UTC),
                        500,
                        "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred.",
                        request.getRequestURI()
                ));
    }
}

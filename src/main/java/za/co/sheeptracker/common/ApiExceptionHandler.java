package za.co.sheeptracker.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {
    public record ApiError(OffsetDateTime timestamp, int status, String error, String message, String path) {
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
}

package com.officebuddy.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        var msg = e.getMessage() != null ? e.getMessage() : "Bad request";
        return ResponseEntity
                .badRequest()
                .body(Map.of("message", msg, "error", msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException e
    ) {
        var errors = new HashMap<String, String>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            var fieldName = ((FieldError) error).getField();
            var errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        var firstMsg = errors.values().stream().findFirst().orElse("Validation failed");
        return ResponseEntity
                .badRequest()
                .body(Map.of("message", firstMsg, "errors", errors.toString(), "error", firstMsg));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.error("Media type not supported", e);
        var msg = "Content-Type '" + e.getContentType() + "' is not supported. Use 'application/json'";
        return ResponseEntity.badRequest().body(Map.of("message", msg, "error", msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception e) {
        log.error("Unhandled exception", e);
        var raw = e.getMessage();
        var msg = (raw != null && !raw.isBlank()) ? raw : "An unexpected error occurred";
        // Truncate to avoid Map.of null issues and huge payloads
        if (msg.length() > 500) msg = msg.substring(0, 500);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", msg, "error", msg));
    }
}

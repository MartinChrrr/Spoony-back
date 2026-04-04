package com.spoony.backend.application.rest.common;

import com.spoony.backend.domain.shared.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<JSendResponse<Map<String, String>>> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: code={} message={}", ex.getCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(JSendResponse.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<JSendResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {
        String field = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField())
                .orElse(null);

        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Erreur de validation");

        log.warn("Validation error: field={} message={}", field, message);

        if (field != null) {
            return ResponseEntity
                    .badRequest()
                    .body(JSendResponse.fail("VALIDATION_ERROR", message, field));
        }
        return ResponseEntity
                .badRequest()
                .body(JSendResponse.fail("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<JSendResponse<Map<String, String>>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(JSendResponse.fail("INVALID_ENUM", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JSendResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .internalServerError()
                .body(JSendResponse.error("Internal server error"));
    }
}

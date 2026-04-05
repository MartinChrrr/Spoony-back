package com.spoony.backend.application.rest.common;

import com.spoony.backend.domain.shared.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<JSendResponse<Map<String, String>>> handleMalformedJson(
            HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON request: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(JSendResponse.fail("MALFORMED_JSON", "Le corps de la requête est invalide ou mal formé"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<JSendResponse<Map<String, String>>> handleMissingParam(
            MissingServletRequestParameterException ex) {
        log.warn("Missing required parameter: {}", ex.getParameterName());
        return ResponseEntity.badRequest()
                .body(JSendResponse.fail("MISSING_PARAMETER",
                        "Paramètre obligatoire manquant : " + ex.getParameterName()));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<JSendResponse<Map<String, String>>> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex) {
        log.warn("Unsupported media type: {}", ex.getContentType());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(JSendResponse.fail("UNSUPPORTED_MEDIA_TYPE",
                        "Content-Type non supporté. Utilisez application/json"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JSendResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .internalServerError()
                .body(JSendResponse.error("Une erreur inattendue est survenue"));
    }
}

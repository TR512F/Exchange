package com.bubnov.v5.config;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> {
                    return error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value";
                })
                .collect(Collectors.joining(", "));

        Map<String, String> response = new HashMap<>();
        response.put("message", errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        if (ex.getRequiredType().equals(BigDecimal.class)) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid value for amount. It should be a valid number.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Invalid input data.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex, WebRequest request) {
        Map<String, Object> errorDetails = new HashMap<>();

        errorDetails.put("message", ex.getMessage());

        HttpStatus status;

        if (ex instanceof IllegalArgumentException || ex instanceof IllegalStateException) {
            status = HttpStatus.BAD_REQUEST;
        } else if (ex instanceof NullPointerException) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ResponseEntity.status(status).body(errorDetails);
    }
}

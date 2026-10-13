package com.college.placement_backend;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import io.swagger.v3.oas.annotations.Hidden;
import com.college.placement_backend.exception.IneligibleCGPAException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler{
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message){
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(IneligibleCGPAException.class)
    public ResponseEntity<Map<String, Object>> handleIneligibleCGPA(IneligibleCGPAException e){
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    // Handle empty fields, etc...
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException e){
        String errorMsg = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorMsg);
    }

    @ExceptionHandler(Exception.class)
    // Fallbacks, any other crashes
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception e){
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }
}
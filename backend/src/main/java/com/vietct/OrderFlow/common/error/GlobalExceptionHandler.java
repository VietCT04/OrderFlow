package com.vietct.OrderFlow.common.error;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.vietct.OrderFlow.catalog.exception.CategoryNotFoundException;
import com.vietct.OrderFlow.catalog.exception.ProductNotFoundException;
import com.vietct.OrderFlow.inventory.exception.InsufficientStockException;
import com.vietct.OrderFlow.inventory.exception.InventoryNotFoundException;
import com.vietct.OrderFlow.order.exception.OrderNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404 – Not Found (domain “not found” exceptions)
    @ExceptionHandler({
            ProductNotFoundException.class,
            CategoryNotFoundException.class,
            OrderNotFoundException.class,
            InventoryNotFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException ex,
                                                           HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        // log as info/warn (no stack trace spam for expected 404)
        log.info("404 Not Found at {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(status).body(body);
    }

    // 400 – Bean validation failures on @RequestBody (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                         HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                "Validation failed for request body",
                request.getRequestURI(),
                fieldErrors
        );

        log.info("400 Validation error at {}: {}", request.getRequestURI(), fieldErrors);

        return ResponseEntity.status(status).body(body);
    }

    // 400 – Validation failures on @RequestParam / @PathVariable (@Validated, ConstraintViolation)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                      HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String path = violation.getPropertyPath().toString();
            fieldErrors.put(path, violation.getMessage());
        }

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                "Validation failed for request parameters",
                request.getRequestURI(),
                fieldErrors
        );

        log.info("400 Constraint violation at {}: {}", request.getRequestURI(), fieldErrors);

        return ResponseEntity.status(status).body(body);
    }

    // 2) 409 – Business conflict: insufficient stock

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientStock(InsufficientStockException ex,
                                                                    HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(status).body(body);
    }

    // 500 – Catch-all for unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex,
                                                             HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI(),
                null
        );

        // log with full stack trace for debugging
        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.status(status).body(body);
    }
}

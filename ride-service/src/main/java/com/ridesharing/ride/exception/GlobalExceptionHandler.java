package com.ridesharing.ride.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(RideNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            RideNotFoundException ex) {

        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }
	
	@ExceptionHandler(InvalidRideRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalid(
            InvalidRideRequestException ex) {

        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
	
	@ExceptionHandler(RideConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(
            RideConflictException ex) {

        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

	 @ExceptionHandler(ServiceUnavailableException.class)
	    public ResponseEntity<Map<String, Object>> handleUnavailable(
	            ServiceUnavailableException ex) {

	        return error(
	            HttpStatus.SERVICE_UNAVAILABLE,
	            ex.getMessage()
	        );
	    }
	 
	 @ExceptionHandler(MethodArgumentNotValidException.class)
	    public ResponseEntity<Map<String, Object>> handleValidation(
	            MethodArgumentNotValidException ex) {

	        Map<String, String> errors = new LinkedHashMap<>();

	        ex.getBindingResult().getFieldErrors().forEach(field ->
	            errors.putIfAbsent(
	                field.getField(),
	                field.getDefaultMessage()
	            )
	        );

	        Map<String, Object> body = new LinkedHashMap<>();
	        body.put("status", 400);
	        body.put("message", "Validation failed");
	        body.put("errors", errors);

	        return ResponseEntity.badRequest().body(body);
	    }
	 
	 @ExceptionHandler({
	        HttpMessageNotReadableException.class,
	        MethodArgumentTypeMismatchException.class
	    })
	    public ResponseEntity<Map<String, Object>> handleBadInput(
	            Exception ex) {

	        return error(
	            HttpStatus.BAD_REQUEST,
	            "Invalid JSON or request parameter"
	        );
	    }

	 
	 private ResponseEntity<Map<String, Object>> error(
	            HttpStatus status, String message) {

	        Map<String, Object> body = new LinkedHashMap<>();

	        body.put("status", status.value());
	        body.put("message", message);

	        return ResponseEntity.status(status).body(body);
	    }
}

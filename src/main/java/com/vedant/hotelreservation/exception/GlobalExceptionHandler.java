package com.vedant.hotelreservation.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(ApiException.class)
	ResponseEntity<ErrorResponse> handleApi(ApiException exception) {
		return ResponseEntity.status(exception.getStatus()).body(new ErrorResponse(exception.getStatus().value(), exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining(", "));
		return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException exception) {
		return ResponseEntity.badRequest().body(new ErrorResponse(400, exception.getMessage()));
	}

	@ExceptionHandler(AuthenticationException.class)
	ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException exception) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(401, "Invalid credentials"));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, "A record with the same unique value already exists"));
	}

	record ErrorResponse(int status, String message, Instant timestamp) {
		ErrorResponse(int status, String message) { this(status, message, Instant.now()); }
	}
}

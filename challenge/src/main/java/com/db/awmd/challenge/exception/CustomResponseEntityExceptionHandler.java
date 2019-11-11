package com.db.awmd.challenge.exception;

import javax.validation.ConstraintViolationException;

import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.db.awmd.challenge.model.ErrorDetails;

import lombok.extern.slf4j.Slf4j;

/**
 * Custom exception handler for all controllers.
 */
@Slf4j
@ControllerAdvice
@RestController
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	/**
	 * Exception mapping for ConstraintViolationException. It handles all the
	 * standard java validation annotations except @valid.
	 *
	 * @param ex
	 *            ConstraintViolationException
	 * @param request
	 *            request
	 * @return ErrorDetails
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorDetails> handleConstraintViolationException(ConstraintViolationException ex,
			WebRequest request) {
		log.warn("ConstraintViolationException occurred. Exception message: {}", ex.getMessage());

		final String[] message = new String[1];

		try {
			ex.getConstraintViolations().forEach(constraintViolation -> {
				ConstraintViolationImpl violation = (ConstraintViolationImpl) constraintViolation;
				PathImpl path = (PathImpl) violation.getPropertyPath();
				message[0] = path.getLeafNode().asString() + " " + constraintViolation.getMessage();
			});
		} catch (Exception e) {
			message[0] = ex.getMessage();
		}

		ErrorDetails errorDetails = new ErrorDetails(HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(), message[0], request.getDescription(false));

		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}
}

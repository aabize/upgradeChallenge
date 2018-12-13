package com.campsite.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { ReservationNotFoundException.class })
    protected ResponseEntity<Object> handleNotFound(
            RuntimeException ex, WebRequest request) {

        ApiError error = new ApiError();
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setMessage(ex.getMessage());
        error.setError("Not found");
        error.setPath(request.getDescription(false).replaceAll("uri=", ""));
        error.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = { InvalidDateException.class, NotAvailableException.class, InvalidReservationException.class })
    protected ResponseEntity<Object> handleInvalidDate(
            RuntimeException ex, WebRequest request) {

        ApiError error = new ApiError();
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setMessage(ex.getMessage());
        error.setError("Bad request");
        error.setPath(request.getDescription(false).replaceAll("uri=", ""));
        error.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        ApiError error = new ApiError();
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setMessage("Parameter " + "'" + ((MethodArgumentTypeMismatchException) ex).getName() + "' has an invalid format");
        error.setError("Type mismatch");
        error.setPath(request.getDescription(false).replaceAll("uri=", ""));
        error.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        ApiError error = new ApiError();
        error.setStatus(HttpStatus.BAD_REQUEST.value());

        try {
            error.setMessage("Field " + "'" + ((InvalidFormatException) ex.getCause()).getPath().get(0).getFieldName()
                    + "' has an invalid value: " + "'" + ((InvalidFormatException) ex.getCause()).getValue().toString() + "'");
        } catch (Exception e) {}

        error.setError("Message not readable");
        error.setPath(request.getDescription(false).replaceAll("uri=", ""));
        error.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


}

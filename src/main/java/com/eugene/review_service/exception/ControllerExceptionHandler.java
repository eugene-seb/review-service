package com.eugene.review_service.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URISyntaxException;
import java.util.Date;

@RestControllerAdvice
public class ControllerExceptionHandler {

    private static String getCauseMessage(Throwable cause) {
        return cause != null ? cause
                .getCause()
                .getMessage() : "No cause available";
    }

    @ExceptionHandler(DuplicatedException.class)
    public ResponseEntity<ErrorMessage> duplicatedExceptionHandler(
            DuplicatedException ex, WebRequest request) {


        ErrorMessage errorMessage = new ErrorMessage(HttpStatus.CONFLICT.value(), new Date(),
                ex.getMessage(), getCauseMessage(ex.getCause()), request.getDescription(false));

        return new ResponseEntity<>(errorMessage, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorMessage> resourceNotFoundExceptionHandler(
            NotFoundException ex, WebRequest request) {

        ErrorMessage errorMessage = new ErrorMessage(HttpStatus.NOT_FOUND.value(), new Date(),
                ex.getMessage(), getCauseMessage(ex.getCause()), request.getDescription(false));

        return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {JsonProcessingException.class, URISyntaxException.class, IllegalArgumentException.class, Exception.class})
    public ResponseEntity<ErrorMessage> globalExceptionHandler(
            Exception ex, WebRequest request) {

        ErrorMessage errorMessage = new ErrorMessage(HttpStatus.BAD_REQUEST.value(), new Date(),
                ex.getMessage(), getCauseMessage(ex.getCause()), request.getDescription(false));

        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
}

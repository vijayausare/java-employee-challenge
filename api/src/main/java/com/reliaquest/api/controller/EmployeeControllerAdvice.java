package com.reliaquest.api.controller;

import com.reliaquest.api.exception.APIException;
import java.util.concurrent.CompletionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class EmployeeControllerAdvice {
    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<?> handleException(RuntimeException ex) {
        log.error("Error handling API request: ", ex);
        return ResponseEntity.internalServerError().body(ex.getMessage());
    }

    @ExceptionHandler(APIException.class)
    protected ResponseEntity<?> handleAPIException(APIException ex) {
        log.error("Error handling API request: ", ex);
        return ResponseEntity.status(ex.statusCode).body(ex.getMessage());
    }

    @ExceptionHandler(CompletionException.class)
    protected ResponseEntity<?> handleCompletionException(CompletionException ex) {
        log.error("Error handling API request: ", ex);
        return ResponseEntity.status(((APIException) ex.getCause()).statusCode)
                .body(ex.getCause().getMessage());
    }
}

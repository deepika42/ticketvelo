package com.deepika.ticketvelo.config;

import com.deepika.ticketvelo.common.exception.TicketVeloException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handle our Custom Logic Exceptions
    @ExceptionHandler(TicketVeloException.class)
    public ResponseEntity<ProblemDetail> handleTicketException(TicketVeloException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        problem.setTitle("Business Logic Error");
        problem.setType(URI.create("https://ticketvelo.com/errors/" + ex.getClass().getSimpleName()));
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(ex.getStatus()).body(problem);
    }

    // 2. Handle Unexpected System Crashes (Catch-All)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneralException(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please contact support."
        );
        problem.setTitle("System Failure");
        ex.printStackTrace();

        return ResponseEntity.internalServerError().body(problem);
    }
}
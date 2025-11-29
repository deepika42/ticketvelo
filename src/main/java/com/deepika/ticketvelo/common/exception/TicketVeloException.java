package com.deepika.ticketvelo.common.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class TicketVeloException extends RuntimeException {
    private final HttpStatus status;

    public TicketVeloException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
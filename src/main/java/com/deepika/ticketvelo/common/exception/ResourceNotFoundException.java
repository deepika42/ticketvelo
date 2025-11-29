package com.deepika.ticketvelo.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends TicketVeloException {
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND); // 404 Not Found
    }
}
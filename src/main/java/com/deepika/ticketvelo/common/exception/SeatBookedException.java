package com.deepika.ticketvelo.common.exception;

import org.springframework.http.HttpStatus;

public class SeatBookedException extends TicketVeloException {
    public SeatBookedException(String message) {
        super(message, HttpStatus.CONFLICT); // 409 Conflict
    }
}
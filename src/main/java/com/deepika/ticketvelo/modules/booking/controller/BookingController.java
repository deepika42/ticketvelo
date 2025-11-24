package com.deepika.ticketvelo.modules.booking.controller;

import com.deepika.ticketvelo.modules.booking.model.Ticket;
import com.deepika.ticketvelo.modules.booking.repository.TicketRepository;
import com.deepika.ticketvelo.modules.booking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    @Autowired
    private TicketRepository ticketRepository;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public Ticket bookTicket(@RequestBody BookingRequest request) {
        return bookingService.bookTicket(request.eventId(), request.seatId(), request.userId());
    }

    // DTO: Simple container for the JSON data
    public record BookingRequest(Long eventId, Long seatId, Long userId) {}

    @GetMapping("/event/{eventId}")
    public List<Ticket> getTicketsForEvent(@PathVariable Long eventId) {
        return ticketRepository.findByEventId(eventId);
    }
}
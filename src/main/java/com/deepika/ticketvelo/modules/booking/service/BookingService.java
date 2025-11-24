package com.deepika.ticketvelo.modules.booking.service;

import com.deepika.ticketvelo.modules.booking.model.Ticket;
import com.deepika.ticketvelo.modules.booking.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;

@Service
public class BookingService {

    private final TicketRepository ticketRepository;

    public BookingService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public Ticket bookTicket(Long eventId, Long seatId, Long userId) {
        try {
            Optional<Ticket> ticketOptional = ticketRepository.findByEventIdAndSeatId(eventId, seatId);

            if (ticketOptional.isEmpty()) {
                throw new RuntimeException("Ticket not found!");
            }

            Ticket ticket = ticketOptional.get();

            if (!"AVAILABLE".equals(ticket.getStatus())) {
                throw new RuntimeException("Ticket is already booked!");
            }

            ticket.setStatus("BOOKED");
            ticket.setUserId(userId);

            return ticketRepository.save(ticket);

        } catch (ObjectOptimisticLockingFailureException e) {
            // Catch the specific version conflict!
            throw new RuntimeException("Too slow! Someone else booked this ticket.");
        }
    }
}
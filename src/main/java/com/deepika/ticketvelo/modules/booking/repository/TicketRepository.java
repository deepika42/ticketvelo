package com.deepika.ticketvelo.modules.booking.repository;

import com.deepika.ticketvelo.modules.booking.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Custom Query: Find a ticket by the Event and the specific Seat
    // We need this to see if Seat 1A is available for Event X
    Optional<Ticket> findByEventIdAndSeatId(Long eventId, Long seatId);

    //Query: Find all tickets for a particular event
    List<Ticket> findByEventId(Long eventId);
}
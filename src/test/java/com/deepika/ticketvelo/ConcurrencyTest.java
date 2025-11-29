package com.deepika.ticketvelo;

import com.deepika.ticketvelo.modules.booking.service.BookingService;
import com.deepika.ticketvelo.modules.catalog.model.Event;
import com.deepika.ticketvelo.modules.catalog.model.Seat;
import com.deepika.ticketvelo.modules.catalog.model.Venue;
import com.deepika.ticketvelo.modules.booking.model.Ticket;
import com.deepika.ticketvelo.modules.catalog.repository.EventRepository;
import com.deepika.ticketvelo.modules.catalog.repository.SeatRepository;
import com.deepika.ticketvelo.modules.catalog.repository.VenueRepository;
import com.deepika.ticketvelo.modules.booking.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
//@Transactional
public class ConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private VenueRepository venueRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private TicketRepository ticketRepository;

    @Test
    public void testDoubleBooking() throws InterruptedException {
        // 1. Setup: Create a single "Golden Ticket"
        Venue venue = venueRepository.save(new Venue());
        Event event = new Event();
        event.setVenue(venue);
        event.setDate(LocalDateTime.now());
        eventRepository.save(event);

        Seat seat = new Seat("A", 1, "VIP", venue);
        seatRepository.save(seat);

        Ticket ticket = new Ticket();
        ticket.setEvent(event);
        ticket.setSeat(seat);
        ticket.setStatus("AVAILABLE");
        ticketRepository.save(ticket);

        Long eventId = event.getId();
        Long seatId = seat.getId();

        // 2. The Attack: 10 threads trying to book the same ticket at once
        int numberOfThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        System.out.println("STARTING CONCURRENCY TEST...");

        for (int i = 0; i < numberOfThreads; i++) {
            long userId = i;
            executor.submit(() -> {
                try {
                    bookingService.bookTickets(eventId, List.of(seatId), userId);
                    System.out.println("User " + userId + " booked the ticket!");
                } catch (Exception e) {
                    System.out.println("User " + userId + " failed: " + e.getMessage());
                }
            });
        }

        // 3. Wait for all threads to finish
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // 4. Check the result
        Ticket finalTicket = ticketRepository.findById(ticket.getId()).get();
        System.out.println("🏁 Final Ticket Owner: User " + finalTicket.getUserId());
    }
}
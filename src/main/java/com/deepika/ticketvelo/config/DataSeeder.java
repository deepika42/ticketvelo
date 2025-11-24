package com.deepika.ticketvelo.config;

import com.deepika.ticketvelo.modules.booking.model.Ticket;
import com.deepika.ticketvelo.modules.booking.repository.TicketRepository;
import com.deepika.ticketvelo.modules.catalog.model.Event;
import com.deepika.ticketvelo.modules.catalog.model.Seat;
import com.deepika.ticketvelo.modules.catalog.model.Venue;
import com.deepika.ticketvelo.modules.catalog.repository.EventRepository;
import com.deepika.ticketvelo.modules.catalog.repository.SeatRepository;
import com.deepika.ticketvelo.modules.catalog.repository.VenueRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;

    public DataSeeder(VenueRepository venueRepository, EventRepository eventRepository, SeatRepository seatRepository, TicketRepository ticketRepository) {
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Check if data already exists so we don't duplicate it every time we restart
        if (venueRepository.count() > 0) {
            System.out.println("Data already exists. Skipping.");
            return;
        }

        // 2. Create a Venue
        Venue venue = new Venue();
        venue.setName("Madison Square Garden");
        venue.setAddress("New York, NY");
        venue.setCapacity(100);
        venueRepository.save(venue);

        // 3. Create an Event
        Event event = new Event();
        event.setTitle("Java Concurrency Masterclass");
        event.setDate(LocalDateTime.now().plusDays(30));
        event.setVenue(venue);
        eventRepository.save(event);

        // 4. Generate 100 Seats (10 Rows x 10 Seats)
        // 3. Create Seats AND Tickets
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 10; j++) {
                // Create Seat
                Seat seat = new Seat("Row-" + i, j, "General", venue);
                seatRepository.save(seat);

                // NEW: Create the Ticket immediately as "AVAILABLE"
                Ticket ticket = new Ticket();
                ticket.setEvent(event);
                ticket.setSeat(seat);
                ticket.setStatus("AVAILABLE");
                ticket.setUserId(null); // No owner yet
                ticketRepository.save(ticket);
            }
        }

        System.out.println("Database Seeded: Venue, Event, Seats, and 100 AVAILABLE Tickets.");
    }
}
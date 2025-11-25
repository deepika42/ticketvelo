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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;

    public DataSeeder(VenueRepository venueRepository, EventRepository eventRepository,
                      SeatRepository seatRepository, TicketRepository ticketRepository) {
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (venueRepository.count() > 0) return;

        System.out.println("Seeding Realistic Venues (Batch Mode)...");
        long startTime = System.currentTimeMillis();

        // Use Case 1: Large Concert Arena (2,500 Seats)
        createVenueWithBatching("The Mega Dome", "Los Angeles", "Beyoncé Renaissance Tour", 50, 50);

        // Use Case 2: Mid-Sized Theater (500 Seats)
        createVenueWithBatching("Broadway Theater", "New York", "Hamilton", 20, 25);

        long endTime = System.currentTimeMillis();
        System.out.println("Database Seeded in " + (endTime - startTime) + "ms.");
    }

    @Transactional
    public void createVenueWithBatching(String venueName, String address, String eventTitle, int rows, int seatsPerRow) {
        // 1. Create Venue
        Venue venue = new Venue();
        venue.setName(venueName);
        venue.setAddress(address);
        venue.setCapacity(rows * seatsPerRow);
        venueRepository.save(venue);

        // 2. Create Event
        Event event = new Event();
        event.setTitle(eventTitle);
        event.setDate(LocalDateTime.now().plusDays(30));
        event.setVenue(venue);
        eventRepository.save(event);

        // 3. Generate Objects in Memory (The "Batch" List)
        List<Seat> seatBatch = new ArrayList<>();
        List<Ticket> ticketBatch = new ArrayList<>();

        for (int r = 1; r <= rows; r++) {
            // Generate Row Label: 1->A, 26->Z, 27->AA
            String rowLabel = generateRowLabel(r);

            for (int s = 1; s <= seatsPerRow; s++) {
                Seat seat = new Seat(rowLabel, s, "Standard", venue);
                seatBatch.add(seat);
            }
        }

        // 4. BULK SAVE Seats (1 Database Call instead of 2500)
        List<Seat> savedSeats = seatRepository.saveAll(seatBatch);

        // 5. Link Tickets to the SAVED seats
        for (Seat seat : savedSeats) {
            Ticket ticket = new Ticket();
            ticket.setEvent(event);
            ticket.setSeat(seat);
            ticket.setStatus("AVAILABLE");
            ticketBatch.add(ticket);
        }

        // 6. BULK SAVE Tickets
        ticketRepository.saveAll(ticketBatch);

        System.out.println("   -> Created " + venueName + " with " + (rows * seatsPerRow) + " seats.");
    }

    // Helper to turn 1->A, 27->AA
    private String generateRowLabel(int n) {
        StringBuilder result = new StringBuilder();
        while (n > 0) {
            n--;
            result.insert(0, (char) ('A' + (n % 26)));
            n /= 26;
        }
        return result.toString();
    }
}
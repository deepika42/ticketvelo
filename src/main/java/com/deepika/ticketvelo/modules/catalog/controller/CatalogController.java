package com.deepika.ticketvelo.modules.catalog.controller;

import com.deepika.ticketvelo.modules.catalog.model.Event;
import com.deepika.ticketvelo.modules.catalog.model.Venue;
import com.deepika.ticketvelo.modules.catalog.repository.EventRepository;
import com.deepika.ticketvelo.modules.catalog.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/catalog") // Base URL for this controller
public class CatalogController {

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private EventRepository eventRepository;

    // 1. Create a Venue
    @PostMapping("/venues")
    public Venue createVenue(@RequestBody Venue venue) {
        return venueRepository.save(venue);
    }

    // 2. Create an Event
    @PostMapping("/events")
    public Event createEvent(@RequestBody Event event) {
        return eventRepository.save(event);
    }

    // 3. See all Events
    @GetMapping("/events")
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @GetMapping("/events/{id}")
    public Event getEventById(@PathVariable Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
    }
}
package com.deepika.ticketvelo.modules.booking.service;

import com.deepika.ticketvelo.modules.booking.model.Ticket;
import com.deepika.ticketvelo.modules.booking.repository.TicketRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final TicketRepository ticketRepository;
    private final StringRedisTemplate redisTemplate; // Native Redis Tool
    private final KafkaTemplate<String, String> kafkaTemplate; // 1. Inject Kafka

    public BookingService(TicketRepository ticketRepository, StringRedisTemplate redisTemplate, KafkaTemplate<String, String> kafkaTemplate) {
        this.ticketRepository = ticketRepository;
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<Ticket> bookTickets(Long eventId, List<Long> seatIds, Long userId) {
        Collections.sort(seatIds);
        List<String> acquiredLocks = new ArrayList<>();

        try {
            // 1. Try to Lock ALL seats first
            for (Long seatId : seatIds) {
                String lockKey = "lock:event:" + eventId + ":seat:" + seatId;

                Boolean lockAcquired = redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(5)); // 5s lock

                if (Boolean.FALSE.equals(lockAcquired)) {
                    throw new RuntimeException("Seat " + seatId + " is currently selected by another user.");
                }
                acquiredLocks.add(lockKey);
            }

            // 2. All locks acquired! Proceed to DB transaction
            return processBookingBatch(eventId, seatIds, userId);

        } finally {
            // 3. Always cleanup locks
            for (String key : acquiredLocks) {
                redisTemplate.delete(key);
            }
        }
    }

    @Transactional
    protected List<Ticket> processBookingBatch(Long eventId, List<Long> seatIds, Long userId) {
        List<Ticket> bookedTickets = new ArrayList<>();

        for (Long seatId : seatIds) {
            // Reuse your existing logic logic per seat
            Optional<Ticket> ticketOptional = ticketRepository.findByEventIdAndSeatId(eventId, seatId);

            if (ticketOptional.isEmpty()) throw new RuntimeException("Ticket not found: " + seatId);

            Ticket ticket = ticketOptional.get();
            if (!"AVAILABLE".equals(ticket.getStatus())) {
                throw new RuntimeException("One of the seats is already taken!");
            }

            ticket.setStatus("BOOKED");
            ticket.setUserId(userId);
            bookedTickets.add(ticketRepository.save(ticket));

            // Send Kafka Event (Simulated)
            String message = "Ticket Confirmed: " + ticket.getId() + " for User " + userId;
            kafkaTemplate.send("ticket-updates", message);
        }

        return bookedTickets;
    }

    @Transactional
    protected Ticket processBooking(Long eventId, Long seatId, Long userId) {
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

        Ticket savedTicket = ticketRepository.save(ticket);

        // 2. Fire an event to Kafka
        // Message Format: "TicketID:UserEmail"
        String message = "Ticket Confirmed: " + savedTicket.getId() + " for User " + userId;
        kafkaTemplate.send("ticket-updates", message);
        System.out.println("Event published to Kafka: " + message);

        return savedTicket;
    }
}
package com.deepika.ticketvelo.modules.booking.service;

import com.deepika.ticketvelo.modules.booking.model.Ticket;
import com.deepika.ticketvelo.modules.booking.repository.TicketRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
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

    public Ticket bookTicket(Long eventId, Long seatId, Long userId) {
        // 1. Define the Lock Key
        String lockKey = "lock:event:" + eventId + ":seat:" + seatId;

        // 2. The "Atomic" Lock (SETNX)
        // "Set this key to 'LOCKED' ONLY IF it does not exist yet. Expire in 2 seconds."
        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(2));

        if (Boolean.FALSE.equals(lockAcquired)) {
            // If false, it means someone else holds the lock
            throw new RuntimeException("⚠️ Server is busy: Seat is being processed by another user.");
        }

        try {
            // 3. We have the lock! Process the booking safely.
            return processBooking(eventId, seatId, userId);
        } finally {
            // 4. Release the lock so others can try later
            redisTemplate.delete(lockKey);
        }
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
        System.out.println("📨 Event published to Kafka: " + message);

        return savedTicket;
    }
}
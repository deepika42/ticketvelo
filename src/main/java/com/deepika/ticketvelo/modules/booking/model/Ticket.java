package com.deepika.ticketvelo.modules.booking.model;

import com.deepika.ticketvelo.modules.catalog.model.Event;
import com.deepika.ticketvelo.modules.catalog.model.Seat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tickets")
@Getter @Setter
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which Event is this for?
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    // Which Physical Seat?
    @ManyToOne
    @JoinColumn(name = "seat_id")
    private Seat seat;

    // Status: AVAILABLE, LOCKED, BOOKED
    private String status;

    // Who owns it?
    private Long userId;

    @Version // <--- OPTIMISTIC LOCKING
    private Integer version;
}
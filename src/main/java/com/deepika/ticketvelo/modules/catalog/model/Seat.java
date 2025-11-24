package com.deepika.ticketvelo.modules.catalog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "seats")
@Getter @Setter
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rowNumber;    // e.g., "A", "B", "C"
    private int seatNumber;      // e.g., 1, 2, 3...
    private String section;      // e.g., "VIP", "General"

    @ManyToOne
    @JoinColumn(name = "venue_id")
    @JsonIgnore
    private Venue venue;

    public Seat(String rowNumber, int seatNumber, String section, Venue venue) {
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.section = section;
        this.venue = venue;
    }

    public Seat() {}
}
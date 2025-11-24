package com.deepika.ticketvelo.modules.catalog.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter @Setter
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;           // e.g., "Taylor Swift - Eras Tour"
    private LocalDateTime date;     // e.g., 2025-12-01 20:00:00

    @ManyToOne // 1. Relationship: One Venue hosts Many Events
    @JoinColumn(name = "venue_id") // 2. Creates a "Foreign Key" column in the database
    private Venue venue;
}
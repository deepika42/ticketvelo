package com.deepika.ticketvelo.modules.catalog.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "venues")
@Getter @Setter
public class Venue {

    @Id // 3. Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 4. Auto-increment (1, 2, 3...)
    private Long id;

    private String name;      // e.g., "Madison Square Garden"
    private String address;   // e.g., "4 Penn Plaza, NY"
    private int capacity;     // e.g., 20000
}
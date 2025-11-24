package com.deepika.ticketvelo.modules.catalog.repository;

import com.deepika.ticketvelo.modules.catalog.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    // Empty! JpaRepository gives us .save(), .findById(), .findAll() for free.
}
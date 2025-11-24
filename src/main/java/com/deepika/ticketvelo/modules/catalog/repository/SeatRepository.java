package com.deepika.ticketvelo.modules.catalog.repository;

import com.deepika.ticketvelo.modules.catalog.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
}
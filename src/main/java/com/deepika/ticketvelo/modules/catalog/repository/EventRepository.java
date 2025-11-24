package com.deepika.ticketvelo.modules.catalog.repository;

import com.deepika.ticketvelo.modules.catalog.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
}
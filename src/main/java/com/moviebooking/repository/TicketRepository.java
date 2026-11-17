package com.moviebooking.repository;

import com.moviebooking.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    Optional<Ticket> findByBookingId(Long bookingId);
}

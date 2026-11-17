package com.moviebooking.repository;

import com.moviebooking.model.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    boolean existsByShowIdAndSeatId(Long showId, Long seatId);

    List<BookingSeat> findByShowId(Long showId);

    List<BookingSeat> findByBookingId(Long bookingId);
}

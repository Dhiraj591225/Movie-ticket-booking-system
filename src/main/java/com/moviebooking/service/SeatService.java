
package com.moviebooking.service;

import com.moviebooking.model.Screen;
import com.moviebooking.model.Seat;
import com.moviebooking.repository.ScreenRepository;
import com.moviebooking.repository.SeatRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SeatService {
    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;

    public SeatService(SeatRepository seatRepository, ScreenRepository screenRepository) {
        this.seatRepository = seatRepository;
        this.screenRepository = screenRepository;
    }

    public List<Seat> getAllSeats() {
        return seatRepository.findAll();
    }

    public List<Seat> getSeatsByScreen(Long screenId) {
        return seatRepository.findByScreenId(screenId);
    }

    public Seat addSeat(Long screenId, Seat seat) {
        Screen screen = screenRepository.findById(screenId).orElseThrow(() -> new RuntimeException("Screen not found: " + screenId));

        seat.setId(null);
        seat.setScreen(screen);

        if (seat.getStatus() == null || seat.getStatus().isBlank()) {
            seat.setStatus("AVAILABLE");
        }

        return seatRepository.save(seat);
    }
}


package com.moviebooking.controller;

import com.moviebooking.model.Seat;
import com.moviebooking.service.SeatService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/seats")
public class SeatController {
    private final SeatService service;

    public SeatController(SeatService service) {
        this.service = service;
    }

    @GetMapping
    public List<Seat> getAll() {
        return service.getAllSeats();
    }

    @GetMapping("/screen/{screenId}")
    public List<Seat> getByScreen(@PathVariable Long screenId) {
        return service.getSeatsByScreen(screenId);
    }

    @PostMapping
    public Seat create(@RequestParam Long screenId, @RequestBody Seat seat) {
        return service.addSeat(screenId, seat);
    }
}


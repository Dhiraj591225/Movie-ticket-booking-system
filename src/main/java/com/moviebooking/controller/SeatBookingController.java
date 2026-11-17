package com.moviebooking.controller;

import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.moviebooking.dto.BookingConfirmationResponse;
import com.moviebooking.dto.SeatAvailabilityResponse;
import com.moviebooking.dto.SeatBookingRequest;
import com.moviebooking.model.Booking;
import com.moviebooking.security.CineBookUserPrincipal;
import com.moviebooking.service.BookingService;

@RestController
public class SeatBookingController {
    private final BookingService bookingService;
    public SeatBookingController(BookingService bookingService) { this.bookingService = bookingService; }

    @GetMapping("/shows/{showId}/seats")
    public List<SeatAvailabilityResponse> getSeatAvailability(@PathVariable Long showId) {
        return bookingService.getSeatAvailability(showId);
    }

    @PostMapping("/bookings/with-seats")
    public BookingConfirmationResponse bookSelectedSeats(@Valid @RequestBody SeatBookingRequest request,
            @AuthenticationPrincipal CineBookUserPrincipal principal) {
        if (!"CUSTOMER".equals(principal.getRole())) throw new AccessDeniedException("Only customers can book tickets.");
        Booking booking = bookingService.createBookingWithSeats(request, principal.getUserId());
        return bookingService.getBookingConfirmation(booking.getId());
    }
}

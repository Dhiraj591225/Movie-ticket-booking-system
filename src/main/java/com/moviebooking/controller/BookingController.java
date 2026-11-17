package com.moviebooking.controller;

import com.moviebooking.dto.BookingConfirmationResponse;
import com.moviebooking.model.Booking;
import com.moviebooking.security.CineBookUserPrincipal;
import com.moviebooking.service.BookingService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService service;
    public BookingController(BookingService service) { this.service = service; }

    @GetMapping("/me/detailed")
    public List<BookingConfirmationResponse> getMyBookings(@AuthenticationPrincipal CineBookUserPrincipal principal) {
        return service.getCustomerBookingConfirmations(principal.getUserId());
    }

    @GetMapping("/{id}/confirmation")
    public BookingConfirmationResponse getBookingConfirmation(@PathVariable Long id,
            @AuthenticationPrincipal CineBookUserPrincipal principal) {
        Booking booking = service.getBookingById(id);
        if (!booking.getCustomer().getId().equals(principal.getUserId())) throw new AccessDeniedException("This booking does not belong to your account.");
        return service.getBookingConfirmation(id);
    }
}

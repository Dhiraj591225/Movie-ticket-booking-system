package com.moviebooking.controller;

import com.moviebooking.dto.BookingConfirmationResponse;
import com.moviebooking.model.Ticket;
import com.moviebooking.security.CineBookUserPrincipal;
import com.moviebooking.service.TicketService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;
    public TicketController(TicketService ticketService) { this.ticketService = ticketService; }

    @GetMapping
    public List<Ticket> getAllTickets() { return ticketService.getAllTickets(); }

    @GetMapping("/{id}")
    public Ticket getTicketById(@PathVariable Long id, @AuthenticationPrincipal CineBookUserPrincipal principal) {
        Ticket ticket = ticketService.getTicketById(id);
        verifyOwner(ticket, principal);
        return ticket;
    }

    @GetMapping("/number/{ticketNumber}")
    public Ticket getTicketByNumber(@PathVariable String ticketNumber,
            @AuthenticationPrincipal CineBookUserPrincipal principal) {
        Ticket ticket = ticketService.getTicketByNumber(ticketNumber);
        verifyOwner(ticket, principal);
        return ticket;
    }

    @GetMapping("/booking/{bookingId}")
    public BookingConfirmationResponse getTicketByBookingId(@PathVariable Long bookingId,
            @AuthenticationPrincipal CineBookUserPrincipal principal) {
        BookingConfirmationResponse response = ticketService.getTicketConfirmationByBookingId(bookingId);
        if (!response.getCustomerId().equals(principal.getUserId())) {
            throw new AccessDeniedException("This ticket does not belong to your account.");
        }
        return response;
    }

    private void verifyOwner(Ticket ticket, CineBookUserPrincipal principal) {
        if (ticket.getBooking() == null || ticket.getBooking().getCustomer() == null
                || !ticket.getBooking().getCustomer().getId().equals(principal.getUserId())) {
            throw new AccessDeniedException("This ticket does not belong to your account.");
        }
    }
}

package com.moviebooking.service;

import com.moviebooking.dto.BookingConfirmationResponse;
import com.moviebooking.model.Ticket;
import com.moviebooking.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final PaymentService paymentService;

    public TicketService(TicketRepository ticketRepository, PaymentService paymentService) {
        this.ticketRepository = ticketRepository;
        this.paymentService = paymentService;
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + id));
    }

    public Ticket getTicketByNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketNumber));
    }

    public Ticket getTicketByBookingId(Long bookingId) {
        return ticketRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Ticket not found for booking ID: " + bookingId));
    }

    public BookingConfirmationResponse getTicketConfirmationByBookingId(Long bookingId) {
        Ticket ticket = getTicketByBookingId(bookingId);
        return paymentService.buildConfirmationResponse(ticket.getBooking());
    }
}

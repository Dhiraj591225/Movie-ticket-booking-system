package com.moviebooking.service;

import com.moviebooking.dto.BookingConfirmationResponse;
import com.moviebooking.dto.VirtualPaymentRequest;
import com.moviebooking.model.Booking;
import com.moviebooking.model.BookingSeat;
import com.moviebooking.model.Payment;
import com.moviebooking.model.Ticket;
import com.moviebooking.multithreading.MovieNotificationThread;
import com.moviebooking.repository.BookingRepository;
import com.moviebooking.repository.BookingSeatRepository;
import com.moviebooking.repository.PaymentRepository;
import com.moviebooking.repository.TicketRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final BookingSeatRepository bookingSeatRepository;

    public PaymentService(
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            TicketRepository ticketRepository,
            BookingSeatRepository bookingSeatRepository) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.ticketRepository = ticketRepository;
        this.bookingSeatRepository = bookingSeatRepository;
    }

    // Existing compatibility method
    @Transactional
    public Booking makeVirtualPayment(Long bookingId) {
        return processPaymentEntity(bookingId, "UPI", false);
    }

    // Detailed 1-click payment returning response DTO
    @Transactional
    public BookingConfirmationResponse makeVirtualPaymentDetailed(Long bookingId, String method) {
        String paymentMethod = normalizePaymentMethod(method);
        Booking booking = processPaymentEntity(bookingId, paymentMethod, false);
        return buildConfirmationResponse(booking);
    }

    // Full payment processing workflow with custom details and internal virtual execution
    @Transactional
    public BookingConfirmationResponse processVirtualPayment(VirtualPaymentRequest request, Long authenticatedCustomerId) {
        if (request == null || request.getBookingId() == null) {
            throw new IllegalArgumentException("Booking ID is required for processing payment.");
        }

        String method = normalizePaymentMethod(request.getPaymentMethod());
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found: " + request.getBookingId()));
        if (booking.getCustomer() == null || !booking.getCustomer().getId().equals(authenticatedCustomerId)) {
            throw new org.springframework.security.access.AccessDeniedException("This booking does not belong to your account.");
        }
        Booking paidBooking = processPaymentEntity(request.getBookingId(), method, request.isSimulateFailure());
        return buildConfirmationResponse(paidBooking);
    }

    private Booking processPaymentEntity(Long bookingId, String paymentMethod, boolean simulateFailure) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        if ("CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalArgumentException("This booking is already confirmed and paid for.");
        }

        if (!"PENDING".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalArgumentException("Only pending bookings can be processed for payment. Current status: " + booking.getStatus());
        }

        if (booking.getTotalAmount() <= 0) {
            throw new IllegalArgumentException("Booking total amount must be greater than zero.");
        }

        // Realistic transaction and order references
        long timeNow = System.currentTimeMillis();
        int randomSuffix = (int) (Math.random() * 9000 + 1000);
        String txnId = "TXN-" + timeNow + "-" + randomSuffix;
        String orderId = "ORD-" + timeNow + "-" + booking.getId();

        if (simulateFailure) {
            Payment failedPayment = new Payment(booking, booking.getTotalAmount(), paymentMethod);
            failedPayment.setStatus("FAILED");
            failedPayment.setRazorpayPaymentId(txnId);
            failedPayment.setRazorpayOrderId(orderId);
            failedPayment.setPaymentDate(LocalDateTime.now());
            paymentRepository.save(failedPayment);
            throw new IllegalArgumentException("Payment Declined: The transaction could not be authorized by the issuing bank. Please check your credentials or try another payment method.");
        }

        // Create Payment record (executed internally through the virtual engine)
        Payment payment = new Payment(booking, booking.getTotalAmount(), paymentMethod);
        payment.setStatus("SUCCESS");
        payment.setRazorpayPaymentId(txnId);
        payment.setRazorpayOrderId(orderId);
        payment.setPaymentDate(LocalDateTime.now());

        booking.setPayment(payment);

        // Generate digital Ticket for confirmed booking
        Ticket ticket = new Ticket(booking);
        booking.setTicket(ticket);

        // Mark booking as confirmed
        booking.setStatus("CONFIRMED");

        Booking savedBooking = bookingRepository.save(booking);

        // Multithreading demonstration: Asynchronously trigger confirmation notification thread
        try {
            MovieNotificationThread notificationThread = new MovieNotificationThread();
            notificationThread.start();
        } catch (Exception e) {
            System.err.println("Could not start notification thread: " + e.getMessage());
        }

        return savedBooking;
    }

    private String normalizePaymentMethod(String method) {
        if (method == null || method.trim().isEmpty()) {
            return "UPI";
        }
        String upper = method.trim().toUpperCase();
        if (upper.contains("UPI")) return "UPI";
        if (upper.contains("CARD")) return "CARD";
        if (upper.contains("NET") || upper.contains("BANK")) return "NET_BANKING";
        if (upper.contains("WALLET")) return "WALLET";
        return upper;
    }

    @Transactional(readOnly = true)
    public BookingConfirmationResponse buildConfirmationResponse(Booking booking) {
        BookingConfirmationResponse res = new BookingConfirmationResponse();
        res.setBookingId(booking.getId());
        res.setStatus(booking.getStatus());
        res.setBookingDate(booking.getBookingDate());
        res.setNumberOfTickets(booking.getNumberOfTickets());
        res.setTotalAmount(booking.getTotalAmount());

        if (booking.getCustomer() != null) {
            res.setCustomerId(booking.getCustomer().getId());
            res.setCustomerName(booking.getCustomer().getName());
            res.setCustomerEmail(booking.getCustomer().getEmail());
        }

        if (booking.getShow() != null) {
            res.setShowId(booking.getShow().getId());
            res.setShowDate(booking.getShow().getShowDate());
            res.setShowTime(booking.getShow().getShowTime());
            res.setTicketPrice(booking.getShow().getTicketPrice());

            if (booking.getShow().getMovie() != null) {
                res.setMovieId(booking.getShow().getMovie().getId());
                res.setMovieTitle(booking.getShow().getMovie().getTitle());
                res.setMovieGenre(booking.getShow().getMovie().getGenre());
                res.setDuration(booking.getShow().getMovie().getDuration());
                res.setMoviePoster(booking.getShow().getMovie().getPosterUrl());
            }

            if (booking.getShow().getScreen() != null) {
                res.setScreenName(booking.getShow().getScreen().getName());
                if (booking.getShow().getScreen().getTheatre() != null) {
                    res.setTheatreName(booking.getShow().getScreen().getTheatre().getName());
                } else {
                    res.setTheatreName("CineBook Cinema");
                }
            }
        }

        // Fetch booked seat numbers
        List<BookingSeat> seats = bookingSeatRepository.findByBookingId(booking.getId());
        List<String> seatNumbers = new ArrayList<>();
        for (BookingSeat bs : seats) {
            if (bs.getSeat() != null) {
                seatNumbers.add(bs.getSeat().getSeatNumber());
            }
        }
        res.setSeatNumbers(seatNumbers);

        // Fetch payment details
        Payment payment = booking.getPayment();
        if (payment == null) {
            payment = paymentRepository.findByBookingId(booking.getId()).orElse(null);
        }
        if (payment != null) {
            res.setPaymentId(payment.getId());
            res.setPaymentStatus(payment.getStatus());
            res.setPaymentMethod(payment.getPaymentMethod());
            res.setTransactionId(payment.getRazorpayPaymentId());
            res.setPaymentDate(payment.getPaymentDate());
        }

        // Fetch ticket details
        Ticket ticket = booking.getTicket();
        if (ticket == null) {
            ticket = ticketRepository.findByBookingId(booking.getId()).orElse(null);
        }
        if (ticket != null) {
            res.setTicketId(ticket.getId());
            res.setTicketNumber(ticket.getTicketNumber());
            res.setIssuedAt(ticket.getIssuedAt());
        }

        return res;
    }
}

package com.moviebooking.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moviebooking.dto.BookingConfirmationResponse;
import com.moviebooking.dto.SeatAvailabilityResponse;
import com.moviebooking.dto.SeatBookingRequest;
import com.moviebooking.model.Booking;
import com.moviebooking.model.BookingSeat;
import com.moviebooking.model.Customer;
import com.moviebooking.model.Seat;
import com.moviebooking.model.Show;
import com.moviebooking.model.User;
import com.moviebooking.repository.BookingRepository;
import com.moviebooking.repository.BookingSeatRepository;
import com.moviebooking.repository.ShowRepository;
import com.moviebooking.repository.UserRepository;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final UserRepository userRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PaymentService paymentService;

    public BookingService(
            BookingRepository bookingRepository,
            ShowRepository showRepository,
            UserRepository userRepository,
            BookingSeatRepository bookingSeatRepository,
            PaymentService paymentService) {
        this.bookingRepository = bookingRepository;
        this.showRepository = showRepository;
        this.userRepository = userRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.paymentService = paymentService;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));
    }

    public List<Booking> getBookingsByCustomer(Long customerId) {
        return bookingRepository.findByCustomerIdOrderByIdDesc(customerId);
    }

    @Transactional(readOnly = true)
    public BookingConfirmationResponse getBookingConfirmation(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        return paymentService.buildConfirmationResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingConfirmationResponse> getCustomerBookingConfirmations(Long customerId) {
        List<Booking> bookings = bookingRepository.findByCustomerIdOrderByIdDesc(customerId);
        List<BookingConfirmationResponse> responses = new ArrayList<>();
        for (Booking booking : bookings) {
            responses.add(paymentService.buildConfirmationResponse(booking));
        }
        return responses;
    }

    // Existing method: kept for compatibility.
    @Transactional
    public Booking createBooking(Long customerId, Long showId, int numberOfTickets) {

        if (numberOfTickets <= 0) {
            throw new IllegalArgumentException("Number of tickets must be positive.");
        }

        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));

        if (!(user instanceof Customer)) {
            throw new IllegalArgumentException("The supplied user ID does not belong to a customer.");
        }

        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found: " + showId));
        validateShowNotStarted(show);

        double total = show.getTicketPrice() * numberOfTickets;

        Booking booking = new Booking(
                (Customer) user, show, numberOfTickets, total);

        return bookingRepository.save(booking);
    }

    // Get every seat for the screen assigned to a show.
    @Transactional(readOnly = true)
    public List<SeatAvailabilityResponse> getSeatAvailability(Long showId) {

        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found: " + showId));

        List<BookingSeat> bookedSeats = bookingSeatRepository.findByShowId(showId);

        Set<Long> bookedSeatIds = new HashSet<>();
        for (BookingSeat bookingSeat : bookedSeats) {
            if (bookingSeat.getSeat() != null) {
                bookedSeatIds.add(bookingSeat.getSeat().getId());
            }
        }

        List<SeatAvailabilityResponse> result = new ArrayList<>();

        if (show.getScreen() != null && show.getScreen().getSeats() != null) {
            for (Seat seat : show.getScreen().getSeats()) {
                boolean available = !bookedSeatIds.contains(seat.getId());
                result.add(new SeatAvailabilityResponse(
                        seat.getId(),
                        seat.getSeatNumber(),
                        available,
                        seat.getSeatType()
                ));
            }
        }

        return result;
    }

    // Create a booking for specific seat IDs.
    @Transactional
    public Booking createBookingWithSeats(SeatBookingRequest request, Long authenticatedCustomerId) {

        if (request.getShowId() == null
                || request.getSeatIds() == null
                || request.getSeatIds().isEmpty()) {
            throw new IllegalArgumentException("Show ID and at least one seat are required.");
        }

        List<Long> seatIds = request.getSeatIds();

        if (seatIds.contains(null)) {
            throw new IllegalArgumentException("Seat IDs cannot contain null values.");
        }

        Set<Long> uniqueSeatIds = new HashSet<>(seatIds);

        if (uniqueSeatIds.size() != seatIds.size()) {
            throw new IllegalArgumentException("Duplicate seat IDs are not allowed.");
        }

        User user = userRepository.findById(authenticatedCustomerId)
                .orElseThrow(() -> new RuntimeException("Authenticated customer account was not found."));

        if (!(user instanceof Customer)) {
            throw new IllegalArgumentException("The supplied user ID does not belong to a customer.");
        }

        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new RuntimeException("Show not found: " + request.getShowId()));
        validateShowNotStarted(show);

        List<Seat> screenSeats = show.getScreen().getSeats();
        List<Seat> selectedSeats = new ArrayList<>();

        for (Long seatId : seatIds) {
            Seat selectedSeat = null;

            for (Seat seat : screenSeats) {
                if (seat.getId().equals(seatId)) {
                    selectedSeat = seat;
                    break;
                }
            }

            if (selectedSeat == null) {
                throw new IllegalArgumentException("Seat " + seatId + " does not belong to the screen for this show.");
            }

            boolean alreadyBooked = bookingSeatRepository.existsByShowIdAndSeatId(show.getId(), seatId);

            if (alreadyBooked) {
                throw new IllegalArgumentException("Seat " + selectedSeat.getSeatNumber() + " is already booked for this show.");
            }

            selectedSeats.add(selectedSeat);
        }

        int numberOfTickets = selectedSeats.size();
        double total = show.getTicketPrice() * numberOfTickets;

        Booking booking = new Booking(
                (Customer) user, show, numberOfTickets, total);

        for (Seat seat : selectedSeats) {
            BookingSeat bookingSeat = new BookingSeat();
            bookingSeat.setShow(show);
            bookingSeat.setSeat(seat);
            booking.addBookingSeat(bookingSeat);
        }

        return bookingRepository.save(booking);
    }

    private void validateShowNotStarted(Show show) {
        if (show.getShowDate() == null || show.getShowTime() == null) {
            throw new IllegalArgumentException("Show date and time must be provided.");
        }
        LocalDateTime showStart = LocalDateTime.of(show.getShowDate(), show.getShowTime());

        if (!showStart.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("This show has already started or is in the past.");
        }
    }
}

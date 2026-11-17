
package com.moviebooking.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.moviebooking.exception.BookingNotFoundException;
import com.moviebooking.model.Booking;
import com.moviebooking.repository.BookingRepository;

@RestController
@RequestMapping("/exception-demo")
public class ExceptionHandlingDemoController {

    private final BookingRepository bookingRepository;
    public ExceptionHandlingDemoController(
        BookingRepository bookingRepository) {
            this.bookingRepository = bookingRepository;
    }
    // Demonstrates try, catch, finally,
    // and catching derived/base-class exceptions.
    @GetMapping("/booking/{id}")
    public ResponseEntity<?> testExceptionHandling(@PathVariable Long id) {
        try {
            // Calling a method that declares "throws".
            Booking booking = findBooking(id);
            return ResponseEntity.ok(booking);

        } catch (BookingNotFoundException e) {
            // Catch the specific (derived) exception first.
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage(),"exceptionType","BookingNotFoundException"));

        } catch (RuntimeException e) {
            // Catch a more general exception afterwards.
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(),"exceptionType", "RuntimeException"));

        } finally {
            // Runs whether the try succeeds or an exception occurs.
            System.out.println("Booking lookup operation finished.");
        }
    }

    // Demonstrates throws and throw.
    private Booking findBooking(Long id)
            throws BookingNotFoundException {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Booking ID must be positive.");
        }

        return bookingRepository.findById(id).orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + id));
    }
}


package com.moviebooking.controller;

import com.moviebooking.model.Payment;
import com.moviebooking.repository.PaymentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @GetMapping("/{id}")
    public Payment getPaymentById(@PathVariable Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + id));
    }

    @GetMapping("/booking/{bookingId}")
    public Payment getPaymentByBookingId(@PathVariable Long bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("No payment found for booking ID: " + bookingId));
    }
}

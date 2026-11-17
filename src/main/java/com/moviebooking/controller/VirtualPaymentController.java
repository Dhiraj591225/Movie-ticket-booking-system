package com.moviebooking.controller;

import com.moviebooking.dto.BookingConfirmationResponse;
import com.moviebooking.dto.VirtualPaymentRequest;
import com.moviebooking.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.moviebooking.security.CineBookUserPrincipal;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
public class VirtualPaymentController {

    private final PaymentService paymentService;

    public VirtualPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Process payment with selected method (UPI, Card, Net Banking, Wallet)
     */
    @PostMapping({"/process", "/virtual/process"})
    public ResponseEntity<BookingConfirmationResponse> processPayment(
            @RequestBody VirtualPaymentRequest request,
            @AuthenticationPrincipal CineBookUserPrincipal principal) {
        if (!"CUSTOMER".equals(principal.getRole())) throw new AccessDeniedException("Only customers can pay for bookings.");
        BookingConfirmationResponse response = paymentService.processVirtualPayment(request, principal.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * 1-Click quick payment for a booking
     */
    @PostMapping({"/pay/{bookingId}", "/virtual/{bookingId}"})
    public ResponseEntity<BookingConfirmationResponse> makePayment(
            @PathVariable Long bookingId,
            @RequestParam(required = false, defaultValue = "UPI") String method,
            @AuthenticationPrincipal CineBookUserPrincipal principal) {
        if (!"CUSTOMER".equals(principal.getRole())) throw new AccessDeniedException("Only customers can pay for bookings.");
        BookingConfirmationResponse response = paymentService.processVirtualPayment(
                new VirtualPaymentRequest(bookingId, method), principal.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Get available payment methods
     */
    @GetMapping({"/methods", "/virtual/methods"})
    public ResponseEntity<?> getAvailablePaymentMethods() {
        return ResponseEntity.ok(List.of(
                Map.of(
                        "id", "UPI",
                        "name", "UPI / Dynamic QR Code",
                        "icon", "📱",
                        "description", "Instant payment via Google Pay, PhonePe, Paytm, or UPI ID"
                ),
                Map.of(
                        "id", "CARD",
                        "name", "Debit / Credit Card",
                        "icon", "💳",
                        "description", "Secure checkout with Visa, MasterCard, or RuPay card"
                ),
                Map.of(
                        "id", "NET_BANKING",
                        "name", "Net Banking",
                        "icon", "🏦",
                        "description", "Internet banking through HDFC, SBI, ICICI, Axis, or PNB"
                ),
                Map.of(
                        "id", "WALLET",
                        "name", "CinePay Fast Wallet",
                        "icon", "👛",
                        "description", "1-Click instant checkout using verified wallet balance"
                )
        ));
    }
}

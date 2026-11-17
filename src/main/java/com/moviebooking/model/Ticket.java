
package com.moviebooking.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ticketNumber;

    @OneToOne
    @JoinColumn(name = "booking_id", unique = true)
    @JsonIgnore
    private Booking booking;

    private LocalDateTime issuedAt;

    // Default constructor required by JPA
    public Ticket() {
    }

    // Parameterized constructor
    public Ticket(Booking booking) {
        this.booking = booking;
        this.ticketNumber = UUID.randomUUID().toString();
        this.issuedAt = LocalDateTime.now();
    }

    @PrePersist
    public void initializeTicket() {
        if (ticketNumber == null) {
            ticketNumber = UUID.randomUUID().toString();
        }

        if (issuedAt == null) {
            issuedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }
}


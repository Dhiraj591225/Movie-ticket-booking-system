
package com.moviebooking.model;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.OneToMany;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<BookingSeat> bookingSeats = new ArrayList<>();

    public List<BookingSeat> getBookingSeats() {
        return bookingSeats;
    }

    public void addBookingSeat(BookingSeat bookingSeat) {
        bookingSeats.add(bookingSeat);
        bookingSeat.setBooking(this);
    }

    private LocalDateTime bookingDate;
    private int numberOfTickets;
    private double totalAmount;

    private String status = "PENDING";

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    @JsonIgnore
    private Payment payment;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    @JsonIgnore
    private Ticket ticket;

    public Booking() {
    }

    public Booking(Customer customer, Show show, int numberOfTickets, double totalAmount) {
        this.customer = customer;
        this.show = show;
        this.numberOfTickets = numberOfTickets;
        this.totalAmount = totalAmount;
        this.bookingDate = LocalDateTime.now();
        this.status = "PENDING";
    }

    @PrePersist
    public void setBookingDateBeforeSaving() {
        if (bookingDate == null) {
            bookingDate = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public int getNumberOfTickets() {
        return numberOfTickets;
    }

    public void setNumberOfTickets(int numberOfTickets) {
        this.numberOfTickets = numberOfTickets;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
        if (payment != null) {
            payment.setBooking(this);
        }
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
        if (ticket != null) {
            ticket.setBooking(this);
        }
    }
}



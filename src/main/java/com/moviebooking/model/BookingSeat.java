
package com.moviebooking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(
    name = "booking_seats",
    uniqueConstraints = @UniqueConstraint(name = "uk_show_seat", columnNames = {"show_id", "seat_id"})
)
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonIgnore
    private Booking booking;

    @ManyToOne(optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    public BookingSeat() {
    }

    public Long getId() {
        return id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }
}



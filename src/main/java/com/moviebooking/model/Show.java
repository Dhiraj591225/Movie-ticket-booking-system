
package com.moviebooking.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "movie_shows")
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(optional = false)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    private LocalDate showDate;
    private LocalTime showTime;
    private double ticketPrice;

    public Show() {
    }

    public Show(Movie movie, Screen screen, LocalDate showDate, LocalTime showTime, double ticketPrice) {
        this.movie = movie;
        this.screen = screen;
        this.showDate = showDate;
        this.showTime = showTime;
        this.ticketPrice = ticketPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Screen getScreen() {
        return screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    public LocalDate getShowDate() {
        return showDate;
    }

    public void setShowDate(LocalDate showDate) {
        this.showDate = showDate;
    }

    public LocalTime getShowTime() {
        return showTime;
    }

    public void setShowTime(LocalTime showTime) {
        this.showTime = showTime;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }
}


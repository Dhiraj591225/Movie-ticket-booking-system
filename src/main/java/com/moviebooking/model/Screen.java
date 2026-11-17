
package com.moviebooking.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int capacity;

    // Many screens can belong to one theatre
    @ManyToOne
    @JoinColumn(name = "theatre_id")
    @JsonIgnore
    private Theatre theatre;

    // One screen can contain many seats
    @OneToMany(
        mappedBy = "screen",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Seat> seats = new ArrayList<>();

    // Default constructor required by JPA
    public Screen() {
    }

    // Parameterized constructor
    public Screen(Long id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    public void addSeat(Seat seat) {
        seats.add(seat);
        seat.setScreen(this);
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Theatre getTheatre() {
        return theatre;
    }

    public void setTheatre(Theatre theatre) {
        this.theatre = theatre;
    }
}


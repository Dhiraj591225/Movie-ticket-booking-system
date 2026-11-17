
package com.moviebooking.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Theatre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;

    @OneToMany(
        mappedBy = "theatre",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Screen> screens = new ArrayList<>();

    // Default constructor required by JPA
    public Theatre() {
    }

    // Parameterized constructor
    public Theatre(Long id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public void addScreen(Screen screen) {
        screens.add(screen);
        screen.setTheatre(this);
    }

    public List<Screen> getScreens() {
        return screens;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

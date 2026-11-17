package com.moviebooking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String genre;
    private Integer duration;
    private double rating;
    private String posterUrl;

    // Default constructor
    public Movie() {
    }

    // Parameterized constructor
    public Movie(String title, String genre, int duration, double rating) {
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.rating = rating;
    }

    public Movie(String title, String genre, int duration, double rating, String posterUrl) {
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.rating = rating;
        this.posterUrl = posterUrl;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    // Nested static class for additional movie details
    public static class MovieDetails {

        private String language;
        private String certificate;

        // Constructor
        public MovieDetails(String language, String certificate) {
            this.language = language;
            this.certificate = certificate;
        }

        // Display movie details
        public void displayDetails() {
            System.out.println("Language: " + language);
            System.out.println("Certificate: " + certificate);
        }

        // Getters
        public String getLanguage() {
            return language;
        }

        public String getCertificate() {
            return certificate;
        }
    }
}

package com.moviebooking.service;

import com.moviebooking.model.Movie;
import com.moviebooking.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    // Existing method
    public Movie addMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    // Overloaded method
    public Movie addMovie(String title, String genre, int duration, double rating) {

        Movie movie = new Movie(title, genre, duration, rating);

        return movieRepository.save(movie);
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }
}

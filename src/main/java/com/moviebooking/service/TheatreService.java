
package com.moviebooking.service;

import com.moviebooking.model.Theatre;
import com.moviebooking.repository.TheatreRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TheatreService {
    private final TheatreRepository repository;

    public TheatreService(TheatreRepository repository) {
        this.repository = repository;
    }

    public List<Theatre> getAllTheatres() {
        return repository.findAll();
    }

    public Theatre getTheatreById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Theatre not found: " + id));
    }

    public Theatre addTheatre(Theatre theatre) {
        theatre.setId(null);
        return repository.save(theatre);
    }
}


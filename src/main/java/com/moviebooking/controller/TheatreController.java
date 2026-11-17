
package com.moviebooking.controller;

import com.moviebooking.model.Theatre;
import com.moviebooking.service.TheatreService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/theatres")
public class TheatreController {
    private final TheatreService service;

    public TheatreController(TheatreService service) {
        this.service = service;
    }

    @GetMapping
    public List<Theatre> getAll() {
        return service.getAllTheatres();
    }

    @GetMapping("/{id}")
    public Theatre getById(@PathVariable Long id) {
        return service.getTheatreById(id);
    }

    @PostMapping
    public Theatre create(@RequestBody Theatre theatre) {
        return service.addTheatre(theatre);
    }
}



package com.moviebooking.controller;

import com.moviebooking.dto.ShowRequest;
import com.moviebooking.model.Show;
import com.moviebooking.service.ShowService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/shows")
public class ShowController {
    private final ShowService service;

    public ShowController(ShowService service) {
        this.service = service;
    }

    @GetMapping
    public List<Show> getAll() {
        return service.getAllShows();
    }

    @GetMapping("/{id}")
    public Show getById(@PathVariable Long id) {
        return service.getShowById(id);
    }

    @GetMapping("/movie/{movieId}")
    public List<Show> getByMovie(@PathVariable Long movieId) {
        return service.getShowsByMovie(movieId);
    }
    
    @PostMapping
    public Show create(@RequestBody ShowRequest request) {
        return service.addShow(request);
    }
}

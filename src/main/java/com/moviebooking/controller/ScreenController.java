
package com.moviebooking.controller;

import com.moviebooking.model.Screen;
import com.moviebooking.service.ScreenService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/screens")
public class ScreenController {
    private final ScreenService service;

    public ScreenController(ScreenService service) {
        this.service = service;
    }

    @GetMapping
    public List<Screen> getAll() {
        return service.getAllScreens();
    }

    @PostMapping
    public Screen create(@RequestParam Long theatreId, @RequestBody Screen screen) {
        return service.addScreen(theatreId, screen);
    }
}




package com.moviebooking.service;

import com.moviebooking.model.Screen;
import com.moviebooking.model.Theatre;
import com.moviebooking.repository.ScreenRepository;
import com.moviebooking.repository.TheatreRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ScreenService {
    private final ScreenRepository screenRepository;
    private final TheatreRepository theatreRepository;

    public ScreenService(ScreenRepository screenRepository, TheatreRepository theatreRepository) {
        this.screenRepository = screenRepository;
        this.theatreRepository = theatreRepository;
    }

    public List<Screen> getAllScreens() {
        return screenRepository.findAll();
    }

    public Screen addScreen(Long theatreId, Screen screen) {
        Theatre theatre = theatreRepository.findById(theatreId).orElseThrow(() -> new RuntimeException("Theatre not found: " + theatreId));

        screen.setId(null);
        screen.setTheatre(theatre);
        return screenRepository.save(screen);
    }
}


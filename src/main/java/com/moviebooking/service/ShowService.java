package com.moviebooking.service;

import com.moviebooking.dto.ShowRequest;
import com.moviebooking.model.Show;
import com.moviebooking.model.Movie;
import com.moviebooking.model.Screen;
import com.moviebooking.repository.ShowRepository;
import com.moviebooking.repository.MovieRepository;
import com.moviebooking.repository.ScreenRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShowService {

    private final ShowRepository repository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;

    public ShowService(
            ShowRepository repository,
            MovieRepository movieRepository,
            ScreenRepository screenRepository) {
        this.repository = repository;
        this.movieRepository = movieRepository;
        this.screenRepository = screenRepository;
    }

    public List<Show> getAllShows() {
        return repository.findAll();
    }

    public Show getShowById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Show not found: " + id));
    }

    @Transactional
    public List<Show> getShowsByMovie(Long movieId) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<Show> allShows = repository.findByMovieId(movieId);
        List<Show> upcomingShows = allShows.stream()
                .filter(s -> s.getShowDate() != null && (
                        s.getShowDate().isAfter(today) ||
                        (s.getShowDate().isEqual(today) && s.getShowTime() != null && s.getShowTime().isAfter(now))
                ))
                .sorted((a, b) -> {
                    int dateCmp = a.getShowDate().compareTo(b.getShowDate());
                    if (dateCmp != 0) return dateCmp;
                    return a.getShowTime().compareTo(b.getShowTime());
                })
                .toList();

        // If no active upcoming shows exist for this movie, create fresh shows for Today & Tomorrow
        if (upcomingShows.isEmpty()) {
            Movie movie = movieRepository.findById(movieId).orElse(null);
            List<Screen> screens = screenRepository.findAll();

            if (movie != null && !screens.isEmpty()) {
                Screen screen1 = screens.get(0);
                Screen screen2 = screens.size() > 1 ? screens.get(1) : screen1;
                LocalDate tomorrow = today.plusDays(1);
                LocalDate dayAfter = today.plusDays(2);

                List<Show> generated = new ArrayList<>();
                // Show today later in evening if before 21:00
                if (now.isBefore(LocalTime.of(21, 0))) {
                    LocalTime tonightTime = now.isBefore(LocalTime.of(18, 0)) ? LocalTime.of(19, 30) : LocalTime.of(22, 0);
                    generated.add(new Show(movie, screen1, today, tonightTime, 280.0));
                }

                // Shows tomorrow
                generated.add(new Show(movie, screen1, tomorrow, LocalTime.of(14, 0), 250.0));
                generated.add(new Show(movie, screen2, tomorrow, LocalTime.of(18, 30), 300.0));
                generated.add(new Show(movie, screen1, tomorrow, LocalTime.of(21, 45), 320.0));

                // Show day after tomorrow
                generated.add(new Show(movie, screen2, dayAfter, LocalTime.of(16, 0), 280.0));
                generated.add(new Show(movie, screen1, dayAfter, LocalTime.of(20, 0), 320.0));

                upcomingShows = repository.saveAll(generated);
            }
        }

        return upcomingShows;
    }

    public Show addShow(ShowRequest request) {
        if (request.getMovieId() == null ||
            request.getScreenId() == null ||
            request.getShowDate() == null ||
            request.getShowTime() == null ||
            request.getTicketPrice() <= 0) {
            throw new IllegalArgumentException("Provide valid movie, screen, date, time and ticket price.");
        }

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found: " + request.getMovieId()));

        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new RuntimeException("Screen not found: " + request.getScreenId()));

        Show show = new Show(
                movie,
                screen,
                request.getShowDate(),
                request.getShowTime(),
                request.getTicketPrice()
        );

        return repository.save(show);
    }
}

package com.moviebooking.config;

import com.moviebooking.model.*;
import com.moviebooking.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            MovieRepository movieRepository,
            TheatreRepository theatreRepository,
            ScreenRepository screenRepository,
            SeatRepository seatRepository,
            ShowRepository showRepository,
            UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.movieRepository = movieRepository;
        this.theatreRepository = theatreRepository;
        this.screenRepository = screenRepository;
        this.seatRepository = seatRepository;
        this.showRepository = showRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            seedUsers();
            seedMovies();
            seedTheatreAndScreens();
            seedShowsForAllMovies();
            System.out.println(">>> CineBook DataInitializer completed successfully.");
        } catch (Exception e) {
            System.err.println(">>> DataInitializer warning: " + e.getMessage());
        }
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            Customer customer = new Customer("Demo Customer", "demo@cinebook.com", passwordEncoder.encode("password123"));
            userRepository.save(customer);
            Admin admin = new Admin("Admin CineBook", "admin@cinebook.com", passwordEncoder.encode("admin123"));
            userRepository.save(admin);
            System.out.println("Seeded demo Customer and Admin users with BCrypt passwords.");
        } else {
            // One-time migration for older demo accounts whose passwords were saved as plain text.
            for (User user : userRepository.findAll()) {
                String existing = user.getPassword();
                if (existing != null && !existing.startsWith("$2a$") && !existing.startsWith("$2b$") && !existing.startsWith("$2y$")) {
                    user.setPassword(passwordEncoder.encode(existing));
                    userRepository.save(user);
                }
            }
        }
    }

    private void seedMovies() {
        if (movieRepository.count() == 0) {
            List<Movie> movies = List.of(
                    new Movie("Interstellar", "Sci-Fi / Adventure", 169, 8.7, "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg"),
                    new Movie("Inception", "Action / Sci-Fi", 148, 8.8, "https://image.tmdb.org/t/p/w500/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg"),
                    new Movie("The Dark Knight", "Action / Crime", 152, 9.0, "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg"),
                    new Movie("Avatar: The Way of Water", "Adventure / Sci-Fi", 192, 7.8, "https://image.tmdb.org/t/p/w500/kyeqWdyUXW608qlYkRqosgbbJyK.jpg"),
                    new Movie("Oppenheimer", "Biography / Drama", 180, 8.9, "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg"),
                    new Movie("Avengers: Endgame", "Action / Sci-Fi", 181, 8.4, "https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg")
            );
            movieRepository.saveAll(movies);
            System.out.println("Seeded initial movies catalog.");
        } else {
            // Update existing movies with poster URLs if missing
            List<Movie> existing = movieRepository.findAll();
            for (Movie m : existing) {
                if (m.getPosterUrl() == null || m.getPosterUrl().trim().isEmpty()) {
                    String titleLower = m.getTitle().toLowerCase();
                    if (titleLower.contains("interstellar")) {
                        m.setPosterUrl("https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg");
                    } else if (titleLower.contains("inception")) {
                        m.setPosterUrl("https://image.tmdb.org/t/p/w500/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg");
                    } else if (titleLower.contains("dark knight")) {
                        m.setPosterUrl("https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg");
                    } else if (titleLower.contains("avatar")) {
                        m.setPosterUrl("https://image.tmdb.org/t/p/w500/kyeqWdyUXW608qlYkRqosgbbJyK.jpg");
                    } else if (titleLower.contains("avengers")) {
                        m.setPosterUrl("https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg");
                    } else if (titleLower.contains("3 idiots")) {
                        m.setPosterUrl("https://image.tmdb.org/t/p/w500/7c5LgX8hJ79L0d7w7vM1s1A1m8K.jpg");
                    } else if (titleLower.contains("dangal")) {
                        m.setPosterUrl("https://image.tmdb.org/t/p/w500/jdfhpX0fVqX0XjZ3iQ3s1a2Z8q8.jpg");
                    } else {
                        m.setPosterUrl("https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=600&q=80");
                    }
                    movieRepository.save(m);
                }
            }
        }
    }

    private void seedTheatreAndScreens() {
        Theatre theatre;
        if (theatreRepository.count() == 0) {
            theatre = new Theatre(null, "CineBook Grand Multiplex", "Downtown City Square, Screenplex Plaza");
            theatreRepository.save(theatre);
        } else {
            theatre = theatreRepository.findAll().get(0);
        }

        List<Screen> screens = screenRepository.findAll();
        if (screens.isEmpty()) {
            Screen screen1 = new Screen(null, "Audi 1 (IMAX Laser)", 32);
            screen1.setTheatre(theatre);
            screen1 = screenRepository.save(screen1);
            generateSeatsForScreen(screen1);

            Screen screen2 = new Screen(null, "Audi 2 (Dolby Atmos)", 32);
            screen2.setTheatre(theatre);
            screen2 = screenRepository.save(screen2);
            generateSeatsForScreen(screen2);
        } else {
            // Ensure EVERY screen has full 32 seats
            for (Screen screen : screens) {
                List<Seat> existingSeats = seatRepository.findByScreenId(screen.getId());
                if (existingSeats.size() < 16) {
                    generateSeatsForScreen(screen);
                }
            }
        }
    }

    private void generateSeatsForScreen(Screen screen) {
        String[] rows = {"A", "B", "C", "D"};
        List<Seat> seats = new ArrayList<>();

        for (String row : rows) {
            String seatType = "STANDARD";
            if ("A".equals(row)) {
                seatType = "VIP";
            } else if ("B".equals(row)) {
                seatType = "PREMIUM";
            } else if ("C".equals(row)) {
                seatType = "EXECUTIVE";
            }

            for (int col = 1; col <= 8; col++) {
                String seatNumber = row + col;
                Seat seat = new Seat(seatNumber, seatType, "AVAILABLE");
                seat.setScreen(screen);
                seats.add(seat);
            }
        }
        seatRepository.saveAll(seats);
        System.out.println("Generated " + seats.size() + " cinema seats for " + screen.getName());
    }

    private void seedShowsForAllMovies() {
        List<Movie> movies = movieRepository.findAll();
        List<Screen> screens = screenRepository.findAll();

        if (movies.isEmpty() || screens.isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfter = today.plusDays(2);

        Screen screen1 = screens.get(0);
        Screen screen2 = screens.size() > 1 ? screens.get(1) : screen1;

        List<Show> newShows = new ArrayList<>();

        for (Movie movie : movies) {
            // Check if this movie has any upcoming shows
            List<Show> existingShows = showRepository.findByMovieId(movie.getId());
            boolean hasUpcoming = existingShows.stream().anyMatch(s -> s.getShowDate() != null && (
                    s.getShowDate().isAfter(today) ||
                    (s.getShowDate().isEqual(today) && s.getShowTime() != null && s.getShowTime().isAfter(now))
            ));

            if (!hasUpcoming) {
                // Shows for today (if evening available)
                if (now.isBefore(LocalTime.of(21, 0))) {
                    LocalTime tonight = now.isBefore(LocalTime.of(18, 0)) ? LocalTime.of(19, 30) : LocalTime.of(22, 15);
                    newShows.add(new Show(movie, screen1, today, tonight, 280.0));
                }

                // Shows for tomorrow
                newShows.add(new Show(movie, screen1, tomorrow, LocalTime.of(14, 0), 250.0));
                newShows.add(new Show(movie, screen2, tomorrow, LocalTime.of(18, 30), 300.0));
                newShows.add(new Show(movie, screen1, tomorrow, LocalTime.of(21, 30), 320.0));

                // Shows for day after tomorrow
                newShows.add(new Show(movie, screen2, dayAfter, LocalTime.of(15, 30), 260.0));
                newShows.add(new Show(movie, screen1, dayAfter, LocalTime.of(19, 0), 300.0));
            }
        }

        if (!newShows.isEmpty()) {
            showRepository.saveAll(newShows);
            System.out.println("Seeded " + newShows.size() + " active upcoming shows for movies.");
        }
    }
}

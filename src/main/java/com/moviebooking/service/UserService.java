package com.moviebooking.service;

import com.moviebooking.model.Customer;
import com.moviebooking.model.User;
import com.moviebooking.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() { return repository.findAll(); }

    public User getUserById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    @Transactional
    public Customer registerCustomer(String name, String email, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (repository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        Customer customer = new Customer(name.trim(), normalizedEmail, passwordEncoder.encode(rawPassword));
        return repository.save(customer);
    }
}

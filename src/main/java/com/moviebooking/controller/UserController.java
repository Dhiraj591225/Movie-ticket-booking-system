
package com.moviebooking.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moviebooking.model.User;
import com.moviebooking.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public List<User> getAll() {
        return service.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return service.getUserById(id);
    }
}


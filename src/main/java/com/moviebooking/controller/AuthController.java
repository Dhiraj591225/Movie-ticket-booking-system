package com.moviebooking.controller;

import com.moviebooking.dto.AuthResponse;
import com.moviebooking.dto.LoginRequest;
import com.moviebooking.dto.RegisterRequest;
import com.moviebooking.security.CineBookUserPrincipal;
import com.moviebooking.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contextRepository;

    public AuthController(UserService userService, AuthenticationManager authenticationManager,
                          SecurityContextRepository contextRepository) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.contextRepository = contextRepository;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(@RequestAttribute("_csrf") CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            var customer = userService.registerCustomer(request.getName(), request.getEmail(), request.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponse(customer.getId(), customer.getName(), customer.getEmail(), "CUSTOMER"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(), request.getPassword()));
            if (httpRequest.getSession(false) != null) httpRequest.changeSessionId();
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            contextRepository.saveContext(context, httpRequest, httpResponse);
            return ResponseEntity.ok(toResponse((CineBookUserPrincipal) authentication.getPrincipal()));
        } catch (org.springframework.security.core.AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password."));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CineBookUserPrincipal principal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in."));
        }
        return ResponseEntity.ok(toResponse(principal));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }

    private AuthResponse toResponse(CineBookUserPrincipal user) {
        return new AuthResponse(user.getUserId(), user.getName(), user.getUsername(), user.getRole());
    }
}

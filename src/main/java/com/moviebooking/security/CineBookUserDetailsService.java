package com.moviebooking.security;

import com.moviebooking.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CineBookUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    public CineBookUserDetailsService(UserRepository userRepository) { this.userRepository = userRepository; }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .map(CineBookUserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password."));
    }
}

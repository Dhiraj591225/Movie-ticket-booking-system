package com.moviebooking.security;

import com.moviebooking.model.Customer;
import com.moviebooking.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CineBookUserPrincipal implements UserDetails {
    private final Long userId;
    private final String name;
    private final String email;
    private final String password;
    private final String role;

    public CineBookUserPrincipal(User user) {
        this.userId = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.role = user instanceof Customer ? "CUSTOMER" : "ADMIN";
    }

    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getRole() { return role; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}

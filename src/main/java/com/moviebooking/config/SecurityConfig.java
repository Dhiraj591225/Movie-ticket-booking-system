package com.moviebooking.config;

import com.moviebooking.security.CineBookUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(CineBookUserDetailsService userDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                    SecurityContextRepository contextRepository) throws Exception {
        CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepository.setCookiePath("/");
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfRepository)
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
            .securityContext(context -> context.securityContextRepository(contextRepository))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/login.html", "/register.html", "/style.css", "/script.js", "/auth.js", "/favicon.ico").permitAll()
                .requestMatchers("/auth/me").authenticated()
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/movies/**", "/shows/**", "/screens/**", "/seats/**", "/theatres/**").permitAll()
                .requestMatchers("/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/payments/methods", "/payments/virtual/methods").authenticated()
                .requestMatchers(HttpMethod.GET, "/payments", "/payments/{id}", "/payments/booking/**", "/tickets").hasRole("ADMIN")
                .requestMatchers("/bookings/**", "/payments/**", "/tickets/**").authenticated()
                .anyRequest().authenticated())
            .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, exception) ->
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please log in to continue.")));
        return http.build();
    }
}

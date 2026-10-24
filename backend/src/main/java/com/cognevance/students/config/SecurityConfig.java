package com.cognevance.students.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain applicationSecurity(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    UserDetailsService applicationUser(
            @Value("${app.auth.username}") String username,
            @Value("${app.auth.password}") String password,
            PasswordEncoder passwordEncoder) {
        int passwordBytes = password.getBytes(StandardCharsets.UTF_8).length;
        int passwordCharacters = password.codePointCount(0, password.length());
        if (username.isBlank() || username.contains(":") || passwordCharacters < 16 || passwordBytes > 72) {
            throw new IllegalStateException("APP_AUTH_USERNAME must be set without ':' and APP_AUTH_PASSWORD must contain at least 16 characters and no more than 72 UTF-8 bytes");
        }
        return new InMemoryUserDetailsManager(User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles("ADMIN")
                .build());
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

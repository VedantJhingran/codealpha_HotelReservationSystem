
package com.vedant.hotelreservation.service;

import com.vedant.hotelreservation.domain.User;
import com.vedant.hotelreservation.domain.UserRole;
import com.vedant.hotelreservation.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class AdminInitializer {

    @Bean
    CommandLineRunner createAdmin(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${HOTEL_ADMIN_EMAIL:}") String email,
            @Value("${HOTEL_ADMIN_PASSWORD:}") String password,
            @Value("${HOTEL_ADMIN_NAME:System Administrator}") String name,
            @Value("${HOTEL_ADMIN_PASSWORD_RESET:false}") boolean passwordReset
    ) {
        return args -> {
            initializeAdmin(users, passwordEncoder, email, password, name, passwordReset);
        };
    }

    @Transactional
    void initializeAdmin(UserRepository users, PasswordEncoder passwordEncoder,
            String email, String password, String name, boolean passwordReset) {
        if (email.isBlank() && password.isBlank()) {
            if (passwordReset) {
                throw new IllegalStateException(
                        "HOTEL_ADMIN_EMAIL and HOTEL_ADMIN_PASSWORD are required for a password reset");
            }
            return;
        }
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            throw new IllegalStateException(
                    "HOTEL_ADMIN_EMAIL, HOTEL_ADMIN_PASSWORD, and HOTEL_ADMIN_NAME must all be configured");
        }

        String adminEmail = email.trim().toLowerCase();
        User existing = users.findByEmailIgnoreCase(adminEmail).orElse(null);

        if (existing != null) {
            if (existing.getRole() != UserRole.ADMIN) {
                throw new IllegalStateException(
                        "Configured admin email already belongs to a USER. Use a different email.");
            }
            if (passwordReset) {
                existing.changePassword(passwordEncoder.encode(password));
                users.save(existing);
            }
            return;
        }

        if (passwordReset) {
            throw new IllegalStateException(
                    "Password reset is only allowed for an existing ADMIN account");
        }

        users.save(new User(name.trim(), adminEmail, passwordEncoder.encode(password), null, UserRole.ADMIN));
    }
}

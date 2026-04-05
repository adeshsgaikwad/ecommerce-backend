package com.ecommerce.ecommerce_backend.config;

import com.ecommerce.ecommerce_backend.entity.User;
import com.ecommerce.ecommerce_backend.enums.Role;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Creates admin user on first startup if it doesn't exist
        if (!userRepository.existsByEmail("admin@ecommerce.com")) {
            User admin = User.builder()
                    .name("Admin")
                    .email("admin@ecommerce.com")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .isActive(true)
                    .isEmailVerified(true)
                    .build();
            userRepository.save(admin);
            System.out.println("Admin user created: admin@ecommerce.com / Admin@123");
        }
    }
}
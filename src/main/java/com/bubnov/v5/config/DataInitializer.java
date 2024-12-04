package com.bubnov.v5.config;

import com.bubnov.v5.model.User;
import com.bubnov.v5.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static com.bubnov.v5.model.Role.ROLE_ADMIN;
import static com.bubnov.v5.model.Role.ROLE_USER;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User user = new User();
            user.setUsername("admin");
            user.setRole(ROLE_ADMIN);
            user.setEmail("admin@example.com");
            user.setPassword(passwordEncoder.encode("password"));

            userRepository.save(user);
            System.out.println("Admin user created: admin@example.com / password");
        }
    }
}

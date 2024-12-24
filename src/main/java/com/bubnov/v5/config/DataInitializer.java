package com.bubnov.v5.config;

import com.bubnov.v5.model.Currency;
import com.bubnov.v5.model.User;
import com.bubnov.v5.repository.CurrencyRepository;
import com.bubnov.v5.repository.UserRepository;
import com.bubnov.v5.service.ExchangeRateService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;

import static com.bubnov.v5.model.Role.ROLE_ADMIN;
import static com.bubnov.v5.model.Role.ROLE_USER;

@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;
    private final ExchangeRateService exchangeRateService;

    @Override
    public void run(String... args) {
        initializeCurrencies();
        initializeDatabaseIfNecessary();
        initializeFirstUsers();
    }

    private void initializeCurrencies(){
        if (currencyRepository.count() == 0){
            currencyRepository.saveAll(List.of(
                    new Currency("USD"),
                    new Currency("EUR"),
                    new Currency("GBP"),
                    new Currency("UAH")
            ));
            System.out.println("Currencies initialized.");
            exchangeRateService.putBalance(1000);
        }
    }

    private void initializeDatabaseIfNecessary() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet dataResultSet = statement.executeQuery("SELECT COUNT(*) FROM exchange_rates");
            if (dataResultSet.next() && dataResultSet.getInt(1) == 0) {
                System.out.println("Table 'exchange_rates' is empty. Running initialization script.");

                String script = Files.readString(Paths.get("src/main/resources/data.sql"));

                statement.execute(script);
                System.out.println("Database initialized successfully.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage(), e);
        }
    }

    private void initializeFirstUsers() {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setRole(ROLE_ADMIN);
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("password"));
            userRepository.save(admin);

            User user = new User();
            user.setUsername("user");
            user.setRole(ROLE_USER);
            user.setEmail("user@example.com");
            user.setPassword(passwordEncoder.encode("password"));

            userRepository.save(user);
            System.out.println("Admin user created: admin / password\nTest user created: user / password");
        }
    }
}

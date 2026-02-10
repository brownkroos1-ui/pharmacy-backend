package com.pharmacy.config;

import com.pharmacy.model.Role;
import com.pharmacy.model.User;
import com.pharmacy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean bootstrapEnabled;
    private final String bootstrapUsername;
    private final String bootstrapPassword;
    private final String bootstrapEmail;
    private final String bootstrapName;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.bootstrap.admin.enabled:true}") boolean bootstrapEnabled,
                           @Value("${app.bootstrap.admin.username:admin}") String bootstrapUsername,
                           @Value("${app.bootstrap.admin.password:}") String bootstrapPassword,
                           @Value("${app.bootstrap.admin.email:admin@pharmacy.local}") String bootstrapEmail,
                           @Value("${app.bootstrap.admin.name:Admin}") String bootstrapName) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapEnabled = bootstrapEnabled;
        this.bootstrapUsername = bootstrapUsername;
        this.bootstrapPassword = bootstrapPassword;
        this.bootstrapEmail = bootstrapEmail;
        this.bootstrapName = bootstrapName;
    }

    @Override
    public void run(String... args) {
        if (!bootstrapEnabled) {
            return;
        }

        if (bootstrapUsername == null || bootstrapUsername.isBlank()) {
            System.out.println("Admin bootstrap skipped: username not configured.");
            return;
        }

        if (bootstrapPassword == null || bootstrapPassword.isBlank()) {
            System.out.println("Admin bootstrap skipped: password not configured.");
            return;
        }

        if (userRepository.findByUsername(bootstrapUsername).isEmpty()) {
            User admin = new User();
            admin.setUsername(bootstrapUsername.trim());
            admin.setName(bootstrapName);
            admin.setEmail(bootstrapEmail);
            admin.setPassword(passwordEncoder.encode(bootstrapPassword));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);

            userRepository.save(admin);
            System.out.println("Admin bootstrap user created.");
        } else {
            User admin = userRepository.findByUsername(bootstrapUsername).get();
            boolean updated = false;
            if ((admin.getName() == null || admin.getName().isBlank())
                    && bootstrapName != null && !bootstrapName.isBlank()) {
                admin.setName(bootstrapName);
                updated = true;
            }
            if ((admin.getEmail() == null || admin.getEmail().isBlank())
                    && bootstrapEmail != null && !bootstrapEmail.isBlank()) {
                admin.setEmail(bootstrapEmail);
                updated = true;
            }
            if (updated) {
                userRepository.save(admin);
                System.out.println("Admin bootstrap profile updated with name/email.");
            }
        }
    }
}

package com.pharmacy.config;

import com.pharmacy.model.Role;
import com.pharmacy.model.User;
import com.pharmacy.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setName("Admin");
            admin.setEmail("admin@pharmacy.local");
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);
            System.out.println("Default admin user created: admin / password");
        } else {
            User admin = userRepository.findByUsername("admin").get();
            boolean updated = false;
            if (admin.getName() == null || admin.getName().isBlank()) {
                admin.setName("Admin");
                updated = true;
            }
            if (admin.getEmail() == null || admin.getEmail().isBlank()) {
                admin.setEmail("admin@pharmacy.local");
                updated = true;
            }
            if (updated) {
                userRepository.save(admin);
                System.out.println("Default admin profile updated with name/email");
            }
        }
    }
}

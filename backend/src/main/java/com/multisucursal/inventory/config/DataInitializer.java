package com.multisucursal.inventory.config;

import com.multisucursal.inventory.user.entity.AppUser;
import com.multisucursal.inventory.user.entity.Role;
import com.multisucursal.inventory.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapAdminProperties bootstrapAdminProperties;

    @Override
    public void run(String... args) {
        String adminEmail = bootstrapAdminProperties.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(adminEmail)) {
            return;
        }

        AppUser admin = new AppUser();
        admin.setName(bootstrapAdminProperties.getName());
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(bootstrapAdminProperties.getPassword()));
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);
    }
}


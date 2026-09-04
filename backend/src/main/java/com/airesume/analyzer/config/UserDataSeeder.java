package com.airesume.analyzer.config;

import com.airesume.analyzer.entity.Recruiter;
import com.airesume.analyzer.entity.Role;
import com.airesume.analyzer.entity.User;
import com.airesume.analyzer.repository.RecruiterRepository;
import com.airesume.analyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class UserDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RecruiterRepository recruiterRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Demo user accounts already present in database.");
            return;
        }

        log.info("Seeding demo user accounts (User, Recruiter, Admin)...");

        // 1. Standard Candidate User
        User demoUser = User.builder()
                .fullName("Alex Smith")
                .email("user@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ROLE_USER)
                .status("ACTIVE")
                .build();
        userRepository.save(demoUser);

        // 2. Recruiter User
        User recruiterUser = User.builder()
                .fullName("Sarah Jenkins")
                .email("recruiter@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ROLE_RECRUITER)
                .status("ACTIVE")
                .build();
        User savedRecruiter = userRepository.save(recruiterUser);

        Recruiter recruiterProfile = Recruiter.builder()
                .user(savedRecruiter)
                .companyName("TechCorp Global")
                .companyEmail("recruiter@example.com")
                .industry("Software Architecture")
                .website("https://techcorp.example.com")
                .status("ACTIVE")
                .build();
        recruiterRepository.save(recruiterProfile);

        // 3. System Administrator
        User adminUser = User.builder()
                .fullName("System Administrator")
                .email("admin@example.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ROLE_ADMIN)
                .status("ACTIVE")
                .build();
        userRepository.save(adminUser);

        log.info("Successfully seeded demo user accounts: user@example.com, recruiter@example.com, admin@example.com");
    }
}

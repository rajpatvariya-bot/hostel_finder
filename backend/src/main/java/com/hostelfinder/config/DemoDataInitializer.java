package com.hostelfinder.config;

import com.hostelfinder.entity.User;
import com.hostelfinder.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoDataInitializer {

  @Bean
  CommandLineRunner seedDemoUsers(UserRepository userRepository, PasswordEncoder encoder) {
    return args -> {
      // Ensures login works even if SQL seed hash differs.
      upsertUser(userRepository, encoder, "ADMIN", "Demo Admin", "admin@demo.com", "9999990000");
      upsertUser(userRepository, encoder, "OWNER", "Demo Owner", "owner@demo.com", "9999990001");
      upsertUser(userRepository, encoder, "STUDENT", "Demo Student", "student@demo.com", "9999990002");
    };
  }

  private void upsertUser(
    UserRepository repo,
    PasswordEncoder encoder,
    String role,
    String name,
    String email,
    String phone
  ) {
    String normalized = email.trim().toLowerCase();
    User u = repo.findByEmail(normalized).orElseGet(User::new);
    u.setRole(role);
    u.setName(name);
    u.setEmail(normalized);
    u.setPhone(phone);
    u.setStatus("ACTIVE");
    u.setPasswordHash(encoder.encode("Demo@123"));
    repo.save(u);
  }
}


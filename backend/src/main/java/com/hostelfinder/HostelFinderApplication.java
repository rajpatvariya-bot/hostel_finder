package com.hostelfinder;

import com.hostelfinder.entity.User;
import com.hostelfinder.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class HostelFinderApplication {
  public static void main(String[] args) {
    SpringApplication.run(HostelFinderApplication.class, args);
  }

  @Bean
  public CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    return args -> {
      if (userRepository.findByEmail("admin@test.com").isEmpty()) {
        User admin = new User();
        admin.setName("Master Admin");
        admin.setEmail("admin@test.com");
        admin.setPhone("0000000000");
        admin.setRole("ADMIN");
        admin.setStatus("ACTIVE");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        userRepository.save(admin);
      }
    };
  }
}


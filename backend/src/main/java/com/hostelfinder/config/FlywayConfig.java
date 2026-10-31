package com.hostelfinder.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            try {
                flyway.repair();
            } catch (Exception e) {
                // Ignore if repair not needed
            }
            flyway.migrate();
        };
    }
}

package com.shadownet.nexus.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    private static final Logger logger = LoggerFactory.getLogger(FlywayConfig.class);

    @Value("${app.flyway.fail-fast:false}")
    private boolean failFast;

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            repairFlywayMetadata(flyway);
            migrate(flyway);
        };
    }

    private void repairFlywayMetadata(Flyway flyway) {
        try {
            flyway.repair();
            logger.info("Flyway metadata repair completed");
        } catch (FlywayException ex) {
            logger.warn("Flyway metadata repair failed: {}", ex.getMessage(), ex);
            if (failFast) {
                throw ex;
            }
        }
    }

    private void migrate(Flyway flyway) {
        try {
            flyway.migrate();
            logger.info("Flyway migration completed");
        } catch (FlywayException ex) {
            logger.error("Flyway migration failed: {}", ex.getMessage(), ex);
            if (failFast) {
                throw ex;
            }
            logger.warn("Continuing application startup because app.flyway.fail-fast=false");
        }
    }
}

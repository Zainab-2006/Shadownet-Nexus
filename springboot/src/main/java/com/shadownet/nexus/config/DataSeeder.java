package com.shadownet.nexus.config;

import org.springframework.stereotype.Component;

@Component
public class DataSeeder {
    /*
     * Flyway migrations are the only gameplay content authority.
     *
     * This class intentionally does not seed operators, challenges, or missions:
     * the old fallback data only contained prototype rows and could mask failed
     * migrations with a smaller, conflicting game state.
     */
}
package com.shadownet.nexus.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "timestamp", System.currentTimeMillis(),
                "db", "mysql"));
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        // Simple metrics
        return ResponseEntity.ok(Map.of("uptime", System.currentTimeMillis()));
    }
}
package com.shadownet.nexus.service;

import com.shadownet.nexus.entity.Challenge;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class PCGService {

    private final Random random = new SecureRandom();

    private static final Map<String, String> CATEGORIES = Map.of(
            "web", "Web Exploitation",
            "crypto", "Cryptography",
            "pwn", "Binary Exploitation",
            "forensics", "Digital Forensics",
            "rev", "Reverse Engineering",
            "osint", "Open Source Intelligence",
            "misc", "Miscellaneous");

    private static final String[] DIFFICULTIES = { "easy", "medium", "hard", "insane" };

    private static final int[] POINTS = { 100, 200, 400, 800 };

    public Challenge generateDynamicChallenge(long seed, String sessionId) {
        Random seededRandom = new Random(seed);

        String categoryKey = getRandomKeyFromMap(seededRandom);
        String category = CATEGORIES.get(categoryKey);
        String difficulty = DIFFICULTIES[seededRandom.nextInt(DIFFICULTIES.length)];
        int points = POINTS[seededRandom.nextInt(POINTS.length)];
        String flag = "CTF{pcg_dynamic_" + seed + "_" + categoryKey + difficulty.charAt(0) + "}";
        String description = generateDescription(categoryKey, difficulty, seededRandom);

        Challenge challenge = new Challenge();
        challenge.setId("pcg_" + sessionId + "_" + seed);
        challenge.setName(category + " - " + difficulty.toUpperCase() + " Challenge");
        challenge.setCategory(categoryKey);
        challenge.setDifficulty(difficulty);
        challenge.setPoints(points);
        challenge.setDescription(description);
        challenge.setFlagHash(hashFlag(flag)); // Match GameService hash
        challenge.setCreatedAt(System.currentTimeMillis());
        return challenge;
    }

    private String getRandomKeyFromMap(Random rng) {
        Object[] keys = CATEGORIES.keySet().toArray();
        return (String) keys[rng.nextInt(keys.length)];
    }

    private String generateDescription(String category, String difficulty, Random rng) {
        String[] templates = {
                "Procedurally generated %s challenge. Difficulty: %s. Solve using %s techniques.",
                "Dynamic %s puzzle (seed %d). %s level exploitation required.",
                "%s vulnerability simulation. %s difficulty - think like an attacker."
        };
        String template = templates[rng.nextInt(templates.length)];
        return String.format(template, category, difficulty, category, rng.nextInt(10000), difficulty);
    }

    private String hashFlag(String flag) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(flag.getBytes());
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

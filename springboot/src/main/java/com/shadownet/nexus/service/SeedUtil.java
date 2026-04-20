package com.shadownet.nexus.service;

import java.util.Random;

public final class SeedUtil {

    private SeedUtil() {}

    public static long seedFrom(String... parts) {
        String joined = String.join("|", parts);
        return joined.hashCode() * 31L;
    }

    public static Random seededRandom(long seed) {
        return new Random(seed);
    }

    public static String instanceKey(String prefix, long seed, String sessionId) {
        return prefix + "_" + sessionId.replaceAll("[^A-Za-z0-9_-]", "_") + "_" + Math.abs(seed);
    }
}

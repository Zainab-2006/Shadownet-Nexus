package com.shadownet.nexus.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.shadownet.nexus.entity.Challenge;
import com.shadownet.nexus.repository.ChallengeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DockerManagerService {

    private static final Logger logger = LoggerFactory.getLogger(DockerManagerService.class);

    private DockerClient dockerClient;
    private final Map<String, String> userContainers = new ConcurrentHashMap<>();
    private final Map<String, Integer> containerPorts = new ConcurrentHashMap<>();
    private final AtomicInteger portCounter = new AtomicInteger(8000);

    @Autowired
    private ChallengeRepository challengeRepository;

    @PostConstruct
    public void init() {
        try {
            DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
            ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .maxConnections(100)
                    .connectionTimeout(Duration.ofSeconds(5))
                    .responseTimeout(Duration.ofSeconds(30))
                    .build();

            dockerClient = DockerClientBuilder.getInstance(config)
                    .withDockerHttpClient(httpClient)
                    .build();
            logger.info("DockerManagerService initialized successfully");
        } catch (Throwable t) {
            dockerClient = null;
            logger.warn("DockerManagerService disabled; Docker client initialization failed: {}", t.toString());
        }
    }

    public Map<String, String> spawnChallengeInstance(String userId, String challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId).orElseThrow();

        if (!challenge.getCategory().equals("web") && !challenge.getCategory().equals("pwn")) {
            return Map.of("url", "http://localhost:3001/demo/" + challengeId, "status", "non-docker");
        }

        String containerKey = userId + ":" + challengeId;
        if (userContainers.containsKey(containerKey)) {
            Integer port = containerPorts.get(containerKey);
            if (port != null) {
                return Map.of("url", "http://localhost:" + port, "status", "already_running");
            }
        }

        if (dockerClient == null) {
            return Map.of("error", "Docker not available", "demoUrl", "http://demo.ctf-shadownet.com/" + challengeId);
        }

        try {
            int port = portCounter.getAndIncrement();
            String imageName = "nginx:alpine";

            List<com.github.dockerjava.api.model.PortBinding> portBindings = new ArrayList<>();
            portBindings.add(com.github.dockerjava.api.model.PortBinding.parse("0.0.0.0:" + port + ":80"));

            String containerId = dockerClient.createContainerCmd(imageName)
                    .withName("shadownet-" + containerKey.replace(":", "-"))
                    .withEnv("FLAG=" + getFlagForUser(userId, challengeId))
                    .withPortBindings(portBindings)
                    .exec().getId();

            dockerClient.startContainerCmd(containerId).exec();
            userContainers.put(containerKey, containerId);
            containerPorts.put(containerKey, port);

            logger.info("Pillar 5: Spawned demo container {}:{} -> port {}", containerId, imageName, port);
            Map<String, String> result = new HashMap<>();
            result.put("containerId", containerId);
            result.put("image", imageName);
            result.put("url", "http://localhost:" + port);
            result.put("ttlMinutes", "60");
            result.put("demoMode", "true");
            return result;
        } catch (Exception e) {
            logger.error("Failed to spawn container for user {} challenge {}: {}", userId, challengeId, e.getMessage());
            Map<String, String> errorResult = new HashMap<>();
            errorResult.put("error", "Docker unavailable: " + e.getMessage());
            errorResult.put("demoUrl", "http://demo.ctf-shadownet.com/" + challengeId);
            return errorResult;
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void cleanupStaleContainers() {
        if (dockerClient == null)
            return;
        userContainers.entrySet().stream()
                .filter(entry -> {
                    try {
                        dockerClient.inspectContainerCmd(entry.getValue()).exec();
                        return false;
                    } catch (Exception e) {
                        logger.info("Cleaning up stale container {}", entry.getValue());
                        return true;
                    }
                })
                .map(Map.Entry::getKey)
                .forEach(userContainers::remove);
    }

    private String getFlagForUser(String userId, String challengeId) {
        return "CTF{" + userId.substring(0, 8) + "-" + challengeId + "-" + System.currentTimeMillis() + "}";
    }

    public void stopContainer(String containerKey) {
        String containerId = userContainers.remove(containerKey);
        if (dockerClient != null && containerId != null) {
            try {
                dockerClient.stopContainerCmd(containerId).exec();
                logger.info("Stopped container {}", containerId);
            } catch (Exception e) {
                logger.warn("Failed to stop container {}: {}", containerId, e.getMessage());
            }
        }
    }
}

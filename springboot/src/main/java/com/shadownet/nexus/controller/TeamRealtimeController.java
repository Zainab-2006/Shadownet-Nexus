package com.shadownet.nexus.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class TeamRealtimeController {

    private final SimpMessagingTemplate messagingTemplate;

    public TeamRealtimeController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/team/message")
    public void teamMessage(@Payload Map<String, Object> payload) {
        Object teamId = payload.get("teamId");
        if (teamId == null || String.valueOf(teamId).isBlank()) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/team/" + teamId, Map.of(
                "type", payload.getOrDefault("type", "team:message"),
                "teamId", String.valueOf(teamId),
                "data", payload.getOrDefault("data", Map.of()),
                "timestamp", System.currentTimeMillis()));
    }
}

package com.shadownet.nexus.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadownet.nexus.dto.ChallengeListDTO;
import com.shadownet.nexus.dto.PuzzleChallengeDTO;
import com.shadownet.nexus.dto.PuzzleSessionDTO;
import com.shadownet.nexus.dto.PuzzleStageDTO;
import com.shadownet.nexus.entity.Challenge;
import com.shadownet.nexus.entity.PuzzleSession;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ChallengeViewMapper {
    private static final String DEFAULT_AUTHOR = "NEXUS Team";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChallengeListDTO toListDto(Challenge challenge) {
        return new ChallengeListDTO(
                challenge.getId(),
                challenge.getName(),
                challenge.getCategory(),
                challenge.getDifficulty(),
                challenge.getPoints(),
                challenge.getDescription(),
                0,
                false,
                DEFAULT_AUTHOR,
                hasDockerRuntime(challenge));
    }

    public PuzzleChallengeDTO toPuzzleDto(Challenge challenge) {
        return new PuzzleChallengeDTO(
                challenge.getId(),
                challenge.getName(),
                challenge.getCategory(),
                challenge.getDifficulty(),
                challenge.getPoints(),
                challenge.getDescription(),
                DEFAULT_AUTHOR,
                hasDockerRuntime(challenge),
                toStageDtos(challenge.getStages()));
    }

    public PuzzleSessionDTO toPuzzleSessionDto(PuzzleSession session, Challenge challenge) {
        return new PuzzleSessionDTO(
                session.getId(),
                session.getCurrentStage(),
                session.getHintsUsed(),
                session.isCompleted(),
                toPuzzleDto(challenge));
    }

    private List<PuzzleStageDTO> toStageDtos(String stagesJson) {
        List<PuzzleStageDTO> stages = new ArrayList<>();
        if (stagesJson == null || stagesJson.isBlank()) {
            return stages;
        }

        try {
            JsonNode parsed = objectMapper.readTree(stagesJson);
            if (!parsed.isArray()) {
                return stages;
            }

            for (JsonNode stage : parsed) {
                stages.add(new PuzzleStageDTO(
                        textValue(stage, "briefing"),
                        textValue(stage, "objective"),
                        textValue(stage, "evidence"),
                        textValue(stage, "submitFormat"),
                        textValue(stage, "learningContent")));
            }
        } catch (Exception ignored) {
            return List.of();
        }

        return stages;
    }

    private String textValue(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }

    private boolean hasDockerRuntime(Challenge challenge) {
        return challenge.getDockerImage() != null && !challenge.getDockerImage().isBlank();
    }
}

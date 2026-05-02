package com.shadownet.nexus.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadownet.nexus.dto.PuzzleChallengeDTO;
import com.shadownet.nexus.dto.PuzzleSessionDTO;
import com.shadownet.nexus.entity.Challenge;
import com.shadownet.nexus.entity.PuzzleSession;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChallengeViewMapperTest {
    private final ChallengeViewMapper mapper = new ChallengeViewMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void listDtoDoesNotSerializeSecretChallengeFields() throws Exception {
        String json = objectMapper.writeValueAsString(mapper.toListDto(challenge()));

        assertThat(json).contains("\"id\":\"crypto-001\"");
        assertThat(json).doesNotContain("flagHash", "stages", "hints", "explanation", "dockerImage", "secret-image");
    }

    @Test
    void puzzleDtoSanitizesStageSolutionFields() throws Exception {
        PuzzleChallengeDTO dto = mapper.toPuzzleDto(challenge());
        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("Recover the plaintext", "synt{pnrfne}", "Try all rotations");
        assertThat(json).doesNotContain("flagHash", "answer", "solution", "finalAnswer", "flag{caesar}");
    }

    @Test
    void puzzleSessionDtoDoesNotSerializeRawChallengeEntity() throws Exception {
        PuzzleSession session = new PuzzleSession("user-1", "crypto-001");
        session.setId("session-1");

        PuzzleSessionDTO dto = mapper.toPuzzleSessionDto(session, challenge());
        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":\"session-1\"", "\"currentStage\":1");
        assertThat(json).doesNotContain("flagHash", "answer", "solution", "finalAnswer", "flag{caesar}");
    }

    private Challenge challenge() {
        Challenge challenge = new Challenge();
        challenge.setId("crypto-001");
        challenge.setName("Caesar Cipher");
        challenge.setCategory("crypto");
        challenge.setDifficulty("easy");
        challenge.setPoints(100);
        challenge.setDescription("Decode message.");
        challenge.setFlagHash("hash-value");
        challenge.setStages("""
                [{
                  "briefing": "Recover the plaintext",
                  "objective": "Decode the rotation",
                  "evidence": "synt{pnrfne}",
                  "submitFormat": "flag{decoded_plaintext}",
                  "learningContent": "Try all rotations",
                  "flagHash": "stage-hash",
                  "answer": "flag{caesar}",
                  "solution": "flag{caesar}",
                  "finalAnswer": "flag{caesar}"
                }]
                """);
        challenge.setHints("[{\"content\":\"secret hint\"}]");
        challenge.setExplanation("secret explanation");
        challenge.setDockerImage("secret-image");
        return challenge;
    }
}

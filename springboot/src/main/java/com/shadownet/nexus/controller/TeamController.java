package com.shadownet.nexus.controller;

import com.shadownet.nexus.dto.ErrorResponse;
import com.shadownet.nexus.dto.MemberViewDTO;
import com.shadownet.nexus.dto.TeamSessionViewDTO;
import com.shadownet.nexus.entity.TeamSession;
import com.shadownet.nexus.service.TeamSessionService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/team")
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:5173", "http://localhost:3000" })
public class TeamController {

    @Autowired
    private TeamSessionService teamSessionService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/create")
    public ResponseEntity<?> createTeam(@RequestBody(required = false) Map<String, String> request,
            Authentication auth) {
        try {
            String missionId = request == null ? null : request.get("missionId");
            TeamSession session = teamSessionService.createTeam(auth.getName(), missionId);
            broadcastTeamUpdate(session, "team:create");
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("CREATE_FAILED", "Create failed: " + e.getMessage(), 400));
        }
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<?> getTeamSession(@PathVariable String teamId, Authentication auth) {
        try {
            TeamSession session = teamSessionService.getTeamForUser(teamId, auth.getName());
            TeamSessionViewDTO view = teamSessionService.toViewDTO(session);
            return ResponseEntity.ok(view);
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                    .body(new ErrorResponse("TEAM_FORBIDDEN", "User is not a member of this team session", 403));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("TEAM_NOT_FOUND", "Team session not found", 400));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("SESSION_LOOKUP_FAILED", "Session lookup failed", 500));
        }
    }

    @GetMapping("/{teamId}/members-enriched")
    public ResponseEntity<?> getEnrichedMembers(@PathVariable String teamId, Authentication auth) {
        try {
            TeamSession session = teamSessionService.getTeamForUser(teamId, auth.getName());
            List<MemberViewDTO> members = teamSessionService.getEnrichedMembers(session);
            return ResponseEntity.ok(members);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(new ErrorResponse("TEAM_FORBIDDEN", "Access denied", 403));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("MEMBERS_ERROR", e.getMessage(), 500));
        }
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinTeam(@RequestBody Map<String, String> request, Authentication auth) {
        String teamId = request.get("teamId");
        String userId = auth.getName();
        try {
            TeamSession session = teamSessionService.joinTeam(teamId, userId);
            broadcastTeamUpdate(session, "team:join");
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("JOIN_FAILED", "Join failed: " + e.getMessage(), 400));
        }
    }

    @PostMapping("/{teamId}/ready")
    public ResponseEntity<?> toggleReady(@PathVariable String teamId, @RequestBody Map<String, Boolean> request,
            Authentication auth) {
        String userId = auth.getName();
        boolean ready = request.get("ready");
        try {
            TeamSession session = teamSessionService.toggleReady(teamId, userId, ready);
            broadcastTeamUpdate(session, "team:ready");
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("READY_TOGGLE_FAILED", "Ready toggle failed", 400));
        }
    }

    @PostMapping("/{teamId}/evidence")
    public ResponseEntity<?> addEvidence(@PathVariable String teamId, @RequestBody Map<String, String> request,
            Authentication auth) {
        String userId = auth.getName();
        String evidenceType = request.get("evidenceType");
        try {
            TeamSession session = teamSessionService.addEvidence(teamId, userId, evidenceType);
            broadcastTeamUpdate(session, "team:evidence");
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("EVIDENCE_ADD_FAILED", "Evidence add failed", 400));
        }
    }

    @PostMapping("/{teamId}/start")
    public ResponseEntity<?> start(@PathVariable String teamId, Authentication auth) {
        try {
            TeamSession session = teamSessionService.startTeam(teamId, auth.getName());
            broadcastTeamUpdate(session, "team:start");
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("START_FAILED", "Start failed: " + e.getMessage(), 400));
        }
    }

    @PostMapping("/{teamId}/accuse")
    public ResponseEntity<?> accuse(@PathVariable String teamId, @RequestBody Map<String, String> request,
            Authentication auth) {
        try {
            String accusedId = request.getOrDefault("accusedId", "sable");
            TeamSession session = teamSessionService.accuse(teamId, auth.getName(), accusedId);
            broadcastTeamUpdate(session, "team:accuse");
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("ACCUSE_FAILED", "Accuse failed: " + e.getMessage(), 400));
        }
    }

    private void broadcastTeamUpdate(TeamSession session, String type) {
        messagingTemplate.convertAndSend("/topic/team/" + session.getTeamId(), Map.of(
                "type", type,
                "teamId", session.getTeamId(),
                "status", session.getStatus(),
                "evidenceCount", session.getEvidenceMap().values().stream().mapToInt(Integer::intValue).sum(),
                "timestamp", System.currentTimeMillis()));
    }
}

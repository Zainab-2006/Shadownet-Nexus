package com.shadownet.nexus.controller;

import com.shadownet.nexus.dto.OperatorDto;
import com.shadownet.nexus.dto.OperatorConsequenceRequestDTO;
import com.shadownet.nexus.entity.Operator;
import com.shadownet.nexus.entity.User;
import com.shadownet.nexus.entity.UserOperator;
import com.shadownet.nexus.repository.OperatorRepository;
import com.shadownet.nexus.repository.UserOperatorRepository;
import com.shadownet.nexus.repository.UserRepository;
import com.shadownet.nexus.service.GameplayConsequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class OperatorController {

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private UserOperatorRepository userOperatorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameplayConsequenceService gameplayConsequenceService;

    @GetMapping("/operators")
    public ResponseEntity<List<OperatorDto>> getOperators(Authentication auth) {
        String userId = auth != null ? auth.getName() : null;
        User user = userId != null ? userRepository.findById(userId).orElse(new User()) : new User();
        Integer userScore = user.getScore() != null ? user.getScore() : 0;

        List<Operator> operators = operatorRepository.findAll();
        List<OperatorDto> dtos = operators.stream().map(op -> {
            boolean unlocked = userScore >= op.getUnlockCost();
            UserOperator uo = userId != null ? userOperatorRepository.findByUserIdAndOperatorId(userId, op.getId()) : null;
            boolean selected = uo != null && uo.getSelected();
            String imageBase = "/images/operators/" + op.getId().toLowerCase() + ".png";
            return new OperatorDto(
                    op.getId(), op.getName(), op.getRole(), op.getAbilities(),
                    op.getUnlockCost(), op.getBackstory(), unlocked, selected,
                    imageBase,
                    imageBase
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/operators/select")
    public ResponseEntity<Map<String, Object>> selectOperator(@RequestBody Map<String, String> body,
            Authentication auth) {
        String userId = auth.getName();
        String operatorId = body.get("operatorId");

        User user = userRepository.findById(userId).orElse(null);
        Operator operator = operatorRepository.findById(operatorId).orElse(null);
        if (user == null || operator == null || user.getScore() < operator.getUnlockCost()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot select operator"));
        }

        userOperatorRepository.findByUserId(userId).forEach(uo -> {
            uo.setSelected(false);
            userOperatorRepository.save(uo);
        });

        UserOperator uo = userOperatorRepository.findByUserIdAndOperatorId(userId, operatorId);
        if (uo == null) {
            uo = new UserOperator();
            uo.setUserId(userId);
            uo.setOperatorId(operatorId);
        }
        uo.setSelected(true);
        userOperatorRepository.save(uo);

        user.setSelectedOperator(operatorId);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "selectedOperator", operatorId));
    }

    @PostMapping("/operators/{operatorId}/consequence")
    public ResponseEntity<?> applyOperatorConsequence(@PathVariable String operatorId,
            @RequestBody OperatorConsequenceRequestDTO request,
            Authentication auth) {
        return ResponseEntity.ok(gameplayConsequenceService.applyOperatorConsequence(
                auth.getName(),
                operatorId,
                request));
    }
}

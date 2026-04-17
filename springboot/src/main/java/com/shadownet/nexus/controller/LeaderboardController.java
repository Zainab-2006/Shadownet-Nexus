package com.shadownet.nexus.controller;

import com.shadownet.nexus.entity.Solve;
import com.shadownet.nexus.entity.User;
import com.shadownet.nexus.repository.SolveRepository;
import com.shadownet.nexus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api")
public class LeaderboardController {

    @Autowired
    private SolveRepository solveRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard() {
        Map<String, List<Solve>> solvesByUser = solveRepository.findAll().stream()
                .collect(Collectors.groupingBy(Solve::getUserId));

        List<User> users = userRepository.findAll().stream()
                .sorted(Comparator
                        .comparing((User user) -> user.getScore() == null ? 0 : user.getScore()).reversed()
                        .thenComparing(User::getDisplayName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        List<Map<String, Object>> leaderboard = IntStream.range(0, users.size())
                .mapToObj(index -> {
                    User user = users.get(index);
                    List<Solve> solves = solvesByUser.getOrDefault(user.getId(), List.of());
                    int score = user.getScore() != null
                            ? user.getScore()
                            : solves.stream().mapToInt(Solve::getPoints).sum();

                    return Map.<String, Object>of(
                            "id", user.getId(),
                            "userId", user.getId(),
                            "displayName", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername(),
                            "score", score,
                            "solves", solves.size(),
                            "rank", index + 1);
                })
                .toList();

        return ResponseEntity.ok(leaderboard);
    }
}
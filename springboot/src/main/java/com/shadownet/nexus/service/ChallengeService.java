package com.shadownet.nexus.service;

import com.shadownet.nexus.dto.ChallengeSubmitRequest;
import com.shadownet.nexus.dto.ChallengeSubmitResponse;
import com.shadownet.nexus.entity.Challenge;
import com.shadownet.nexus.entity.Solve;
import com.shadownet.nexus.entity.User;
import com.shadownet.nexus.repository.ChallengeRepository;
import com.shadownet.nexus.repository.SolveRepository;
import com.shadownet.nexus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ChallengeService {

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private SolveRepository solveRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ChallengeSubmitResponse submitFlag(ChallengeSubmitRequest request, String username) {
        Challenge challenge = challengeRepository.findById(request.getChallengeId().toString())
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (solveRepository.existsByUserIdAndChallengeId(user.getId(), request.getChallengeId().toString())) {
            return new ChallengeSubmitResponse(false, 0, "Already solved");
        }

        boolean isCorrect = passwordEncoder.matches(request.getFlag(), challenge.getFlagHash());
        if (isCorrect) {
            Solve solve = new Solve();
            solve.setUserId(user.getId());
            solve.setChallengeId(challenge.getId());
            solve.setPoints(challenge.getPoints());
            solve.setTimestamp(System.currentTimeMillis());
            solveRepository.save(solve);

            return new ChallengeSubmitResponse(true, challenge.getPoints(), "Correct!");
        }

        return new ChallengeSubmitResponse(false, 0, "Incorrect flag");
    }
}

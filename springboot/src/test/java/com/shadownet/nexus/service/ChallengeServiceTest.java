package com.shadownet.nexus.service;

import com.shadownet.nexus.dto.ChallengeSubmitRequest;
import com.shadownet.nexus.dto.ChallengeSubmitResponse;
import com.shadownet.nexus.entity.Challenge;
import com.shadownet.nexus.entity.User;
import com.shadownet.nexus.repository.ChallengeRepository;
import com.shadownet.nexus.repository.SolveRepository;
import com.shadownet.nexus.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {
    
    @Mock
    private ChallengeRepository challengeRepository;
    
    @Mock
    private SolveRepository solveRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private ChallengeService challengeService;
    
    @Test
    void submitFlag_WithCorrectFlag_ShouldAwardPoints() {
        Challenge challenge = new Challenge();
        challenge.setId("chal-1");
        challenge.setPoints(100);
        challenge.setFlagHash("$2a$10$hashedflag");
        
        User user = new User();
        user.setId("testuser");
        user.setUsername("testuser");
        
        ChallengeSubmitRequest request = new ChallengeSubmitRequest();
        request.setChallengeId("chal-1");
        request.setFlag("correct_flag");
        
        when(challengeRepository.findById("chal-1")).thenReturn(Optional.of(challenge));
        when(userRepository.findByUsername(eq("testuser"))).thenReturn(user);
        when(solveRepository.existsByUserIdAndChallengeId(eq("testuser"), eq("chal-1"))).thenReturn(false);
        when(passwordEncoder.matches("correct_flag", challenge.getFlagHash())).thenReturn(true);
        
        ChallengeSubmitResponse response = challengeService.submitFlag(request, "testuser");
        
        
        assertThat(response.getCorrect()).isTrue();
        assertThat(response.getPointsEarned()).isEqualTo(100);
        verify(solveRepository).save(any());
    }
    
    @Test
    void submitFlag_WithIncorrectFlag_ShouldNotAwardPoints() {
        Challenge challenge = new Challenge();
        challenge.setId("chal-1");
        challenge.setFlagHash("$2a$10$hashedflag");
        
        User user = new User();
        user.setId("testuser");
        user.setUsername("testuser");
        
        ChallengeSubmitRequest request = new ChallengeSubmitRequest();
        request.setChallengeId("chal-1");
        request.setFlag("wrong_flag");
        
        when(challengeRepository.findById("chal-1")).thenReturn(Optional.of(challenge));
        when(userRepository.findByUsername(eq("testuser"))).thenReturn(user);
        when(passwordEncoder.matches("wrong_flag", challenge.getFlagHash())).thenReturn(false);
        
        ChallengeSubmitResponse response = challengeService.submitFlag(request, "testuser");
        
        
        assertThat(response.getCorrect()).isFalse();
        assertThat(response.getPointsEarned()).isZero();
        verify(solveRepository, never()).save(any());
    }
    
    @Test
    void submitFlag_WhenAlreadySolved_ShouldNotAwardAgain() {
        Challenge challenge = new Challenge();
        challenge.setId("chal-1");
        
        User user = new User();
        user.setId("testuser");
        user.setUsername("testuser");
        
        ChallengeSubmitRequest request = new ChallengeSubmitRequest();
        request.setChallengeId("chal-1");
        request.setFlag("any_flag");
        
        when(challengeRepository.findById("chal-1")).thenReturn(Optional.of(challenge));
        when(userRepository.findByUsername(eq("testuser"))).thenReturn(user);
        when(solveRepository.existsByUserIdAndChallengeId(eq("testuser"), eq("chal-1"))).thenReturn(true);
        
        ChallengeSubmitResponse response = challengeService.submitFlag(request, "testuser");
        
        assertThat(response.getCorrect()).isFalse();
        assertThat(response.getMessage()).containsIgnoringCase("already solved");
        verify(solveRepository, never()).save(any());
    }
}

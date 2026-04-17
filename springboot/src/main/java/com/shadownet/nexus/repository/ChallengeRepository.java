package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, String> {

}
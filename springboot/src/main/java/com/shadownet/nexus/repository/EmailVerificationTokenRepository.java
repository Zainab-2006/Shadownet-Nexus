package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, String> {
    EmailVerificationToken findByTokenHash(String tokenHash);

    EmailVerificationToken findByUserId(String userId);
}

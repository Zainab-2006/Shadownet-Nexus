package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
    PasswordResetToken findByTokenHash(String tokenHash);

    PasswordResetToken findFirstByUser_IdOrderByCreatedAtDesc(String userId);

    void deleteByUser_Id(String userId);
}

package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    RefreshToken findByTokenHash(String tokenHash);

}
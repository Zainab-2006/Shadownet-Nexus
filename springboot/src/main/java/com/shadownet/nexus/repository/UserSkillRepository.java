package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.UserSkill;
import com.shadownet.nexus.entity.UserSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, UserSkillId> {

    List<UserSkill> findByUserId(String userId);

    Optional<UserSkill> findByUserIdAndCategory(String userId, String category);

    @Query("SELECT s FROM UserSkill s WHERE s.userId = :userId ORDER BY s.level DESC")
    List<UserSkill> findTopSkillsByUserId(@Param("userId") String userId);

    default UserSkill getOrCreateSkill(String userId, String category) {
        return findByUserIdAndCategory(userId, category).orElse(new UserSkill(userId, category));
    }
}

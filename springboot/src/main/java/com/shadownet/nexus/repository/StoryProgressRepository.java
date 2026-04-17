package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.StoryProgress;
import com.shadownet.nexus.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StoryProgressRepository extends JpaRepository<StoryProgress, Long> {
    
    Optional<StoryProgress> findByUser(User user);
}


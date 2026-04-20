package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.StoryChoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryChoiceRepository extends JpaRepository<StoryChoice, Long> {
    List<StoryChoice> findByStoryInstanceId(Long storyInstanceId);
}

package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.StoryScene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StorySceneRepository extends JpaRepository<StoryScene, Long> {
    
    Optional<StoryScene> findFirstByChapter_IdOrderBySceneNumberAsc(Long chapterId);
    
    @Query("SELECT s FROM StoryScene s WHERE s.chapter.id = :chapterId ORDER BY s.sceneNumber ASC")
    List<StoryScene> findByChapterIdOrderBySceneNumberAsc(Long chapterId);
}


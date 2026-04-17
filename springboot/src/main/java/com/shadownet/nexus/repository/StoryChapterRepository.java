package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.StoryChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoryChapterRepository extends JpaRepository<StoryChapter, Long> {

    Optional<StoryChapter> findByChapterNumber(Integer chapterNumber);

    @Query("SELECT c FROM StoryChapter c ORDER BY c.chapterNumber ASC")
    List<StoryChapter> findAllByOrderByChapterNumberAsc();

    @Query("SELECT c FROM StoryChapter c WHERE c.chapterNumber > :chapterNumber ORDER BY c.chapterNumber ASC")
    Optional<StoryChapter> findFirstByChapterNumberGreaterThanOrderByChapterNumberAsc(Integer chapterNumber);
}

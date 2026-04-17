-- V25__unlock_story_chapters.sql
-- Current playtest build keeps all authored story chapters available from the start.

UPDATE story_chapters
SET is_locked = b'0',
    required_trust_level = 0
WHERE chapter_number IN (1, 2, 3);

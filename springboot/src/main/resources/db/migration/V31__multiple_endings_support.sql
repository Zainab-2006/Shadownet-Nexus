-- V31__multiple_endings_support.sql
-- Adds backend-backed multiple endings support aligned with the current schema.

ALTER TABLE story_chapters
  ADD COLUMN IF NOT EXISTS ending_key VARCHAR(40) NULL;

ALTER TABLE story_progress
  ADD COLUMN IF NOT EXISTS ending_achieved VARCHAR(40) NULL;

CREATE TABLE IF NOT EXISTS story_ending_definitions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ending_key VARCHAR(40) NOT NULL,
    ending_title VARCHAR(255) NOT NULL,
    ending_description TEXT,
    minimum_trust INT DEFAULT 0,
    required_choices JSON,
    ending_type VARCHAR(40) NOT NULL DEFAULT 'standard',
    unlock_rank INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ending_key (ending_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO story_ending_definitions (ending_key, ending_title, ending_description, minimum_trust, required_choices, ending_type, unlock_rank) VALUES
('ending_victory', 'SHADOWNET NEUTRALIZED', 'You lead the final assault that topples the Shadow Network. Your allies celebrate as the core collapses.', 80, NULL, 'victory', 1),
('ending_pyrrhic', 'PYRRHIC VICTORY', 'ShadowNet falls but trust is shattered. Many allies lost in the final push.', 40, NULL, 'pyrrhic', 2),
('ending_fractured', 'ALLIANCE SHATTERED', 'The team fractures under the pressure. Some walk away, others stay but nothing is the same.', 20, NULL, 'fractured', 3),
('ending_betrayal', 'BETRAYAL REWARDED', 'You turned on NEXUS and joined ShadowNet. Power through betrayal.', -50, NULL, 'betrayal', 4),
('ending_legend', 'LEGEND STATUS', 'Your choices created the ultimate alliance. Everyone survives. The perfect ending.', 100, NULL, 'legend', 0)
ON DUPLICATE KEY UPDATE
  ending_title = VALUES(ending_title),
  ending_description = VALUES(ending_description),
  minimum_trust = VALUES(minimum_trust),
  required_choices = VALUES(required_choices),
  ending_type = VALUES(ending_type),
  unlock_rank = VALUES(unlock_rank);

UPDATE story_chapters
SET ending_key = CASE chapter_number
  WHEN 3 THEN 'ending_victory'
  ELSE ending_key
END
WHERE chapter_number IN (3);

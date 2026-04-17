CREATE TABLE IF NOT EXISTS user_story_evidence (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id VARCHAR(64) NOT NULL,
  evidence_code VARCHAR(120) NOT NULL,
  title VARCHAR(255) NOT NULL,
  summary TEXT NULL,
  source_chapter_id BIGINT NULL,
  source_scene_id BIGINT NULL,
  source_choice_id BIGINT NULL,
  operator_interpretation TEXT NULL,
  mission_relevance_tag VARCHAR(120) NULL,
  discovered_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_story_evidence_code (user_id, evidence_code),
  KEY idx_user_story_evidence_user_time (user_id, discovered_at),
  CONSTRAINT fk_user_story_evidence_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_mission_state (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id VARCHAR(64) NOT NULL,
  mission_id VARCHAR(120) NOT NULL,
  state VARCHAR(40) NOT NULL,
  source_chapter_id BIGINT NULL,
  source_scene_id BIGINT NULL,
  source_choice_id BIGINT NULL,
  reason TEXT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_mission_state (user_id, mission_id),
  KEY idx_user_mission_state_user_time (user_id, updated_at),
  CONSTRAINT fk_user_mission_state_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

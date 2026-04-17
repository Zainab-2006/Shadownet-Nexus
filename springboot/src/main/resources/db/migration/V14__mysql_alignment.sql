-- Align the MySQL schema with the current Spring entities.

ALTER TABLE users
  ADD COLUMN account_locked TINYINT NOT NULL DEFAULT 0,
  ADD COLUMN last_login_at BIGINT NULL;

CREATE TABLE challenge_stages (
  id VARCHAR(64) NOT NULL,
  challenge_id VARCHAR(64) NOT NULL,
  stage_number INT NOT NULL,
  stage_type VARCHAR(50) NOT NULL,
  description TEXT NULL,
  flag_hash VARCHAR(255) NULL,
  hint TEXT NULL,
  points INT NOT NULL DEFAULT 0,
  briefing TEXT NULL,
  learning TEXT NULL,
  created_at BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_challenge_stages_challenge_stage (challenge_id, stage_number),
  CONSTRAINT fk_challenge_stages_challenge FOREIGN KEY (challenge_id) REFERENCES challenges(id) ON DELETE CASCADE
);

CREATE TABLE challenge_sessions (
  id VARCHAR(64) NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  challenge_id VARCHAR(64) NOT NULL,
  operator VARCHAR(64) NULL,
  difficulty VARCHAR(50) NOT NULL DEFAULT 'easy',
  current_stage INT NOT NULL DEFAULT 1,
  status VARCHAR(50) NOT NULL DEFAULT 'active',
  time_started BIGINT NULL,
  time_completed BIGINT NULL,
  total_time INT NULL,
  hints_used INT NOT NULL DEFAULT 0,
  penalty_multiplier DOUBLE NOT NULL DEFAULT 1.0,
  completed TINYINT(1) NOT NULL DEFAULT 0,
  created_at BIGINT NOT NULL,
  seed VARCHAR(64) NULL,
  team_session_id VARCHAR(255) NULL,
  ended_at BIGINT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_challenge_sessions_user_challenge (user_id, challenge_id),
  CONSTRAINT fk_challenge_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_challenge_sessions_challenge FOREIGN KEY (challenge_id) REFERENCES challenges(id) ON DELETE CASCADE
);

CREATE INDEX idx_challenge_session_challenge ON challenge_sessions(challenge_id);
CREATE INDEX idx_challenge_session_user ON challenge_sessions(user_id);
CREATE INDEX idx_challenge_session_team ON challenge_sessions(team_session_id);
CREATE INDEX idx_challenge_session_seed ON challenge_sessions(seed);

CREATE TABLE user_skills (
  user_id VARCHAR(64) NOT NULL,
  category VARCHAR(50) NOT NULL,
  xp INT NOT NULL DEFAULT 0,
  level INT NOT NULL DEFAULT 1,
  updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  last_solve_time BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (user_id, category),
  CONSTRAINT fk_user_skills_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_skills_updated ON user_skills(updated_at);
CREATE INDEX idx_user_skills_xp ON user_skills(xp);

ALTER TABLE challenges
  ADD COLUMN max_solves INT NOT NULL DEFAULT 100,
  ADD COLUMN first_blood_user_id VARCHAR(255) NULL,
  ADD COLUMN first_blood_at BIGINT NULL,
  ADD COLUMN stages JSON NULL,
  ADD COLUMN hints JSON NULL,
  ADD COLUMN explanation TEXT NULL,
  ADD COLUMN docker_image VARCHAR(255) NULL;

CREATE INDEX idx_challenges_max_solves ON challenges(max_solves);
CREATE INDEX idx_challenges_firstblood ON challenges(first_blood_user_id);

CREATE TABLE challenge_instances (
  id VARCHAR(255) NOT NULL,
  session_id VARCHAR(255) NOT NULL,
  container_id VARCHAR(255) NULL,
  port INT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'pending',
  vuln_type VARCHAR(50) NULL,
  target_url VARCHAR(255) NULL,
  flag VARBINARY(512) NULL,
  created_at BIGINT NOT NULL,
  expires_at BIGINT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_challenge_instances_session FOREIGN KEY (session_id) REFERENCES challenge_sessions(id) ON DELETE CASCADE
);

CREATE INDEX idx_challenge_instances_session ON challenge_instances(session_id);
CREATE INDEX idx_challenge_instances_status ON challenge_instances(status);
CREATE INDEX idx_challenge_instances_expires ON challenge_instances(expires_at);

CREATE TABLE puzzle_sessions (
  id VARCHAR(36) NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  challenge_id VARCHAR(64) NOT NULL,
  current_stage INT NOT NULL DEFAULT 1,
  hints_used INT NOT NULL DEFAULT 0,
  completed TINYINT NOT NULL DEFAULT 0,
  created_at BIGINT NULL,
  updated_at BIGINT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_puzzle_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_puzzle_sessions_challenge FOREIGN KEY (challenge_id) REFERENCES challenges(id) ON DELETE CASCADE
);

CREATE INDEX idx_puzzle_sessions_user ON puzzle_sessions(user_id);
CREATE INDEX idx_puzzle_sessions_challenge ON puzzle_sessions(challenge_id);

CREATE TABLE user_events (
  id VARCHAR(36) NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  event_type VARCHAR(50) NOT NULL,
  challenge_id VARCHAR(255) NULL,
  category VARCHAR(50) NULL,
  metadata JSON NULL,
  fail_count INT NOT NULL DEFAULT 0,
  session_duration_seconds INT NOT NULL DEFAULT 0,
  created_at BIGINT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_user_events_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_events_user_time ON user_events(user_id, created_at);
CREATE INDEX idx_user_events_type ON user_events(event_type);
CREATE INDEX idx_user_events_user_category ON user_events(user_id, category);

ALTER TABLE team_sessions
  ADD COLUMN team_id VARCHAR(64) NULL,
  ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'waiting',
  ADD COLUMN traitor_id VARCHAR(64) NULL,
  ADD COLUMN accusation_result VARCHAR(255) NULL,
  ADD COLUMN evidence_json TEXT NULL,
  ADD COLUMN time_started BIGINT NULL;

CREATE TABLE team_members (
  session_id VARCHAR(64) NOT NULL,
  member_id VARCHAR(64) NOT NULL,
  PRIMARY KEY (session_id, member_id),
  CONSTRAINT fk_team_members_session FOREIGN KEY (session_id) REFERENCES team_sessions(id) ON DELETE CASCADE
);

CREATE TABLE trust_relationship (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id VARCHAR(64) NOT NULL,
  target_user_id VARCHAR(64) NOT NULL,
  trust_score INT NOT NULL DEFAULT 0,
  updated_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_trust_relationship_user_target (user_id, target_user_id),
  CONSTRAINT fk_trust_relationship_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_trust_relationship_target_user FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE CASCADE
);

ALTER TABLE audit_logs
  ADD COLUMN action VARCHAR(255) NULL,
  ADD COLUMN entity_type VARCHAR(100) NULL,
  ADD COLUMN entity_id BIGINT NULL,
  ADD COLUMN success TINYINT NOT NULL DEFAULT 1,
  ADD COLUMN user_agent TEXT NULL,
  ADD COLUMN username VARCHAR(255) NULL,
  ADD COLUMN ip_address VARCHAR(45) NULL,
  ADD COLUMN created_at DATETIME NULL;

CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_username ON audit_logs(username);
CREATE INDEX idx_audit_created_at ON audit_logs(created_at);

CREATE TABLE email_verification_tokens (
  id VARCHAR(64) NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  token_hash VARCHAR(255) NOT NULL,
  expires_at BIGINT NOT NULL,
  created_at BIGINT NOT NULL,
  verified TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_email_verification_token_hash (token_hash),
  CONSTRAINT fk_email_verification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE password_reset_tokens (
  id VARCHAR(64) NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  token_hash VARCHAR(255) NOT NULL,
  expires_at BIGINT NOT NULL,
  created_at BIGINT NOT NULL,
  used TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_password_reset_token_hash (token_hash),
  CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE puzzles (
  id VARCHAR(64) NOT NULL,
  mission_id VARCHAR(64) NOT NULL,
  puzzle_type VARCHAR(64) NULL,
  difficulty VARCHAR(50) NULL,
  payload TEXT NULL,
  solution_hash VARCHAR(255) NULL,
  created_at BIGINT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_puzzles_mission FOREIGN KEY (mission_id) REFERENCES missions(id) ON DELETE CASCADE
);

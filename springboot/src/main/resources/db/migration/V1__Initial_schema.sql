-- V1 Initial Shadownet Nexus Schema (MySQL compatible)
CREATE TABLE users (
  id VARCHAR(64) PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  email_hash VARCHAR(64) UNIQUE NOT NULL,
  email_encrypted TEXT,
  username VARCHAR(100) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  display_name VARCHAR(150) NOT NULL,
  score INT DEFAULT 0,
  xp INT DEFAULT 0,
  level INT DEFAULT 1,
  selected_operator VARCHAR(64),
  story_progress JSON,
  failed_login_attempts INT DEFAULT 0,
  last_failed_login_at BIGINT,
  locked_until BIGINT,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  email_verified TINYINT DEFAULT 0,
  email_verify_token_hash VARCHAR(255),
  email_verify_expires BIGINT,
  password_reset_token_hash VARCHAR(255),
  password_reset_expires BIGINT
);

CREATE TABLE operators (
  id VARCHAR(64) PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  role VARCHAR(50) NOT NULL,
  abilities JSON,
  unlock_cost INT DEFAULT 0,
  backstory TEXT
);

CREATE TABLE user_operators (
  user_id VARCHAR(64),
  operator_id VARCHAR(64),
  selected TINYINT DEFAULT 0,
  PRIMARY KEY (user_id, operator_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE
);

CREATE TABLE missions (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(150) NOT NULL,
  mission_type VARCHAR(50) NOT NULL,
  difficulty VARCHAR(50) NOT NULL,
  story JSON,
  meta JSON,
  created_at BIGINT NOT NULL
);

CREATE TABLE challenges (
  id VARCHAR(64) PRIMARY KEY,
  name VARCHAR(255),
  category VARCHAR(50),
  difficulty VARCHAR(50),
  points INT NOT NULL,
  description TEXT,
  flag_hash VARCHAR(255),
  created_at BIGINT NOT NULL,
  solves INT DEFAULT 0
);

CREATE TABLE solves (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id VARCHAR(64) NOT NULL,
  challenge_id VARCHAR(64) NOT NULL,
  points INT NOT NULL,
  timestamp BIGINT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (challenge_id) REFERENCES challenges(id) ON DELETE CASCADE,
  UNIQUE (user_id, challenge_id)
);

CREATE TABLE team_sessions (
  id VARCHAR(64) PRIMARY KEY,
  mission_id VARCHAR(64) NOT NULL,
  state JSON,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  ended_at BIGINT,
  FOREIGN KEY (mission_id) REFERENCES missions(id) ON DELETE CASCADE
);

CREATE TABLE audit_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  event VARCHAR(100) NOT NULL,
  user_id VARCHAR(64),
  ip VARCHAR(45),
  details JSON,
  timestamp BIGINT NOT NULL
);

CREATE TABLE refresh_tokens (
  id VARCHAR(64) PRIMARY KEY,
  user_id VARCHAR(64) NOT NULL,
  token_hash VARCHAR(255) NOT NULL,
  expires BIGINT NOT NULL,
  revoked TINYINT DEFAULT 0,
  created_at BIGINT NOT NULL
);

-- Seed initial data
INSERT INTO operators (id, name, role, abilities, unlock_cost, backstory) VALUES
('op_hacker', 'Cipher', 'Hacker', '["cryptoBreak", "networkScan"]', 0, 'Ex-blackhat turned white-hat.'),
('op_analyst', 'Specter', 'Analyst', '["patternDecode", "dataCorrelate"]', 100, 'Forensic specialist.'),
('op_field', 'Rook', 'Field Agent', '["physicalInfiltration", "droneControl"]', 150, 'Tactical expert.');

INSERT INTO challenges (id, name, category, difficulty, points, description, flag_hash, created_at) VALUES

('web-001', 'Corporate Backdoor', 'web', 'easy', 100, 'Find a way in.', 'sha256_flag_hash_placeholder_web001', 1693488000000),
('crypto-001', 'Broken RSA', 'crypto', 'easy', 100, 'Crack it.', 'sha256_flag_hash_placeholder_crypto001', 1693488000000);


INSERT INTO missions (id, title, mission_type, difficulty, story, meta, created_at) VALUES
('mission_heist_001', 'Data Heist', 'Data Heist', 'medium', '{"synopsis": "Infiltrate and extract data."}', '{"teamSize": 3}', 1693488000000);


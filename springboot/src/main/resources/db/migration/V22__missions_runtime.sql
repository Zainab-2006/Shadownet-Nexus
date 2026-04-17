-- V22__missions_runtime.sql
-- Mission runtime table plus first playable mission set.

CREATE TABLE IF NOT EXISTS missions (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(150),
  mission_type VARCHAR(50),
  difficulty VARCHAR(20),
  story TEXT,
  objectives JSON,
  time_limit_seconds INT DEFAULT 3600,
  xp_reward INT DEFAULT 500,
  meta TEXT,
  created_at BIGINT
);

-- V1 created missions with a narrower JSON contract. Align it before seeding runtime data.
ALTER TABLE missions
  MODIFY COLUMN story TEXT NULL,
  MODIFY COLUMN meta TEXT NULL,
  ADD COLUMN objectives JSON NULL,
  ADD COLUMN time_limit_seconds INT DEFAULT 3600,
  ADD COLUMN xp_reward INT DEFAULT 500;

CREATE TABLE IF NOT EXISTS mission_sessions (
  id VARCHAR(64) PRIMARY KEY,
  user_id VARCHAR(64),
  mission_id VARCHAR(64),
  status VARCHAR(20) DEFAULT 'active',
  progress JSON,
  started_at BIGINT,
  ended_at BIGINT,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (mission_id) REFERENCES missions(id)
);

INSERT IGNORE INTO missions (id, title, mission_type, difficulty, story, objectives, time_limit_seconds, xp_reward, meta, created_at) VALUES
('mission_corp_001', 'Corporate Breach', 'corporate_espionage', 'easy', 'Story Chapter 1: Recruitment leads here.', '["Recon target", "Hack credentials", "Extract data"]', 1800, 300, '{"soloCapable":true,"teamCapable":true}', UNIX_TIMESTAMP() * 1000),
('mission_heist_001', 'Data Heist', 'data_heist', 'medium', 'Disable alarms, coordinate access, and exfiltrate the target archive.', '["Disable alarms", "Team coordination", "Exfil data"]', 2400, 500, '{"soloCapable":true,"teamCapable":true}', UNIX_TIMESTAMP() * 1000),
('mission_infra_001', 'Infrastructure Attack', 'infrastructure_attack', 'medium', 'Map the exposed network and prove controlled access without collateral damage.', '["Map network", "Exploit vulnerability", "Establish controlled access"]', 2700, 500, '{"soloCapable":true,"teamCapable":true}', UNIX_TIMESTAMP() * 1000),
('mission_defense_001', 'Cyber Defense', 'cyber_warfare', 'easy', 'Detect the active breach, contain it, and document remediation steps.', '["Detect attack", "Contain breach", "Remediate host"]', 1800, 300, '{"soloCapable":true,"teamCapable":false}', UNIX_TIMESTAMP() * 1000),
('mission_team_001', 'Team Operation', 'team', 'hard', 'Coordinate shared evidence and resolve the accusation window.', '["Coordinate intel", "Collect evidence", "Accuse leader"]', 3600, 800, '{"soloCapable":false,"teamCapable":true}', UNIX_TIMESTAMP() * 1000),
('mission_final_001', 'ShadowNet Core', 'boss', 'extreme', 'Synthesize the prior evidence chain and neutralize the ShadowNet core.', '["Validate evidence chain", "Execute final breach", "Resolve final accusation"]', 5400, 2000, '{"soloCapable":true,"teamCapable":true}', UNIX_TIMESTAMP() * 1000);

UPDATE missions SET title = 'Corporate Breach', mission_type = 'corporate_espionage', difficulty = 'easy', story = 'Story Chapter 1: Recruitment leads here.', objectives = '["Recon target", "Hack credentials", "Extract data"]', time_limit_seconds = 1800, xp_reward = 300, meta = '{"soloCapable":true,"teamCapable":true}' WHERE id = 'mission_corp_001';
UPDATE missions SET title = 'Data Heist', mission_type = 'data_heist', difficulty = 'medium', story = 'Disable alarms, coordinate access, and exfiltrate the target archive.', objectives = '["Disable alarms", "Team coordination", "Exfil data"]', time_limit_seconds = 2400, xp_reward = 500, meta = '{"soloCapable":true,"teamCapable":true}' WHERE id = 'mission_heist_001';
UPDATE missions SET title = 'Infrastructure Attack', mission_type = 'infrastructure_attack', difficulty = 'medium', story = 'Map the exposed network and prove controlled access without collateral damage.', objectives = '["Map network", "Exploit vulnerability", "Establish controlled access"]', time_limit_seconds = 2700, xp_reward = 500, meta = '{"soloCapable":true,"teamCapable":true}' WHERE id = 'mission_infra_001';
UPDATE missions SET title = 'Cyber Defense', mission_type = 'cyber_warfare', difficulty = 'easy', story = 'Detect the active breach, contain it, and document remediation steps.', objectives = '["Detect attack", "Contain breach", "Remediate host"]', time_limit_seconds = 1800, xp_reward = 300, meta = '{"soloCapable":true,"teamCapable":false}' WHERE id = 'mission_defense_001';
UPDATE missions SET title = 'Team Operation', mission_type = 'team', difficulty = 'hard', story = 'Coordinate shared evidence and resolve the accusation window.', objectives = '["Coordinate intel", "Collect evidence", "Accuse leader"]', time_limit_seconds = 3600, xp_reward = 800, meta = '{"soloCapable":false,"teamCapable":true}' WHERE id = 'mission_team_001';
UPDATE missions SET title = 'ShadowNet Core', mission_type = 'boss', difficulty = 'extreme', story = 'Synthesize the prior evidence chain and neutralize the ShadowNet core.', objectives = '["Validate evidence chain", "Execute final breach", "Resolve final accusation"]', time_limit_seconds = 5400, xp_reward = 2000, meta = '{"soloCapable":true,"teamCapable":true}' WHERE id = 'mission_final_001';
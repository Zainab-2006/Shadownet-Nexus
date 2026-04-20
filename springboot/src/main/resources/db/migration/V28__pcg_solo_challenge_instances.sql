CREATE TABLE pcg_challenge_instances (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  instance_key VARCHAR(120) NOT NULL UNIQUE,
  user_id VARCHAR(64) NOT NULL,
  session_id VARCHAR(120) NOT NULL,
  seed BIGINT NOT NULL,
  mode VARCHAR(20) NOT NULL DEFAULT 'solo',
  category VARCHAR(80) NOT NULL,
  difficulty VARCHAR(40) NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  artifact_json LONGTEXT NULL,
  flag_hash VARCHAR(255) NOT NULL,
  points INT NOT NULL DEFAULT 100,
  attempt_count INT NOT NULL DEFAULT 0,
  hints_used INT NOT NULL DEFAULT 0,
  status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP NULL,
  solved_at TIMESTAMP NULL,
  CONSTRAINT fk_pcg_challenge_instances_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_pcg_challenge_instances_user_status
  ON pcg_challenge_instances(user_id, status);

CREATE INDEX idx_pcg_challenge_instances_session
  ON pcg_challenge_instances(session_id);

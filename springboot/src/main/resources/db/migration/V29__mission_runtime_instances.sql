CREATE TABLE mission_instances (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  instance_key VARCHAR(120) NOT NULL UNIQUE,
  mission_code VARCHAR(80) NOT NULL,
  owner_user_id VARCHAR(64) NOT NULL,
  squad_id VARCHAR(64) NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
  phase VARCHAR(50) NOT NULL DEFAULT 'BRIEFING',
  trust_score INT NOT NULL DEFAULT 0,
  suspicion_score INT NOT NULL DEFAULT 0,
  credits INT NOT NULL DEFAULT 0,
  attempt_count INT NOT NULL DEFAULT 0,
  decisions_count INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP NULL,
  completed_at TIMESTAMP NULL,
  CONSTRAINT fk_mission_instances_owner
    FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE mission_evidence (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  mission_instance_id BIGINT NOT NULL,
  evidence_key VARCHAR(100) NOT NULL,
  evidence_type VARCHAR(60) NOT NULL,
  found BOOLEAN NOT NULL DEFAULT FALSE,
  content_json LONGTEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_mission_evidence_instance
    FOREIGN KEY (mission_instance_id) REFERENCES mission_instances(id) ON DELETE CASCADE
);

CREATE TABLE mission_decisions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  mission_instance_id BIGINT NOT NULL,
  decision_key VARCHAR(100) NOT NULL,
  chosen_option VARCHAR(100) NOT NULL,
  trust_delta INT NOT NULL DEFAULT 0,
  suspicion_delta INT NOT NULL DEFAULT 0,
  credits_delta INT NOT NULL DEFAULT 0,
  consequence_json LONGTEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_mission_decision_instance
    FOREIGN KEY (mission_instance_id) REFERENCES mission_instances(id) ON DELETE CASCADE
);

CREATE INDEX idx_mission_instances_owner_status
  ON mission_instances(owner_user_id, status);

CREATE INDEX idx_mission_instances_squad
  ON mission_instances(squad_id);

CREATE INDEX idx_mission_evidence_instance
  ON mission_evidence(mission_instance_id);

CREATE INDEX idx_mission_decision_instance
  ON mission_decisions(mission_instance_id);

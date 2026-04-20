CREATE TABLE story_instances (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  instance_key VARCHAR(120) NOT NULL UNIQUE,
  user_id VARCHAR(64) NOT NULL,
  operator_code VARCHAR(80) NOT NULL,
  chapter_code VARCHAR(80) NOT NULL,
  scene_code VARCHAR(80) NOT NULL,
  trust_level INT NOT NULL DEFAULT 0,
  affinity_level INT NOT NULL DEFAULT 0,
  status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
  choices_count INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at TIMESTAMP NULL,
  CONSTRAINT fk_story_instances_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE story_choices (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  story_instance_id BIGINT NOT NULL,
  choice_key VARCHAR(100) NOT NULL,
  chosen_option VARCHAR(100) NOT NULL,
  trust_delta INT NOT NULL DEFAULT 0,
  affinity_delta INT NOT NULL DEFAULT 0,
  unlock_scene VARCHAR(100) NULL,
  reward_json LONGTEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_story_choice_instance
    FOREIGN KEY (story_instance_id) REFERENCES story_instances(id) ON DELETE CASCADE
);

CREATE INDEX idx_story_instances_user_status
  ON story_instances(user_id, status);

CREATE INDEX idx_story_instances_user_operator
  ON story_instances(user_id, operator_code);

CREATE INDEX idx_story_choices_instance
  ON story_choices(story_instance_id);

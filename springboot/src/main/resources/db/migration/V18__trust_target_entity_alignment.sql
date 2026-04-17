ALTER TABLE trust_relationship
    DROP FOREIGN KEY fk_trust_relationship_target_user;

ALTER TABLE trust_relationship
    MODIFY COLUMN target_user_id VARCHAR(128) NOT NULL;

-- V11__rate_limit_table.sql

CREATE TABLE IF NOT EXISTS `rate_limit_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `key_name` varchar(255) NOT NULL,
  `action_type` varchar(50) NOT NULL,
  `attempt_count` int NOT NULL DEFAULT '1',
  `first_attempt` datetime,
  `last_attempt` datetime,
  `blocked_until` datetime,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_key_name` (`key_name`),
  KEY `idx_action_type` (`action_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

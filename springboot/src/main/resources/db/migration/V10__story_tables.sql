-- V10__story_tables.sql

CREATE TABLE IF NOT EXISTS `story_chapters` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chapter_number` int NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` TEXT,
  `is_locked` bit(1) NOT NULL DEFAULT b'1',
  `required_trust_level` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chapter_number` (`chapter_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `story_scenes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chapter_id` bigint NOT NULL,
  `scene_number` int NOT NULL,
  `content` TEXT NOT NULL,
  `scene_type` varchar(100),
  `character_speaking` varchar(255) NOT NULL,
  `operator_pov_variants` TEXT,
  `next_scene_id` bigint,
  PRIMARY KEY (`id`),
  KEY `fk_scene_chapter` (`chapter_id`),
  CONSTRAINT `fk_scene_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `story_chapters` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `story_scene_choices` (
  `scene_id` bigint NOT NULL,
  `id` bigint NOT NULL,
  `text` TEXT,
  `trust_impact` int,
  `next_scene_id` bigint,
  PRIMARY KEY (`scene_id`, `id`),
  KEY `fk_choice_next_scene` (`next_scene_id`),
  CONSTRAINT `fk_choice_scene` FOREIGN KEY (`scene_id`) REFERENCES `story_scenes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `story_progress` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255) NOT NULL,
  `current_chapter_id` bigint,
  `current_scene_id` bigint,
  `completed_chapters` JSON,
  `choices_made` JSON,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_progress_user` (`user_id`),
  KEY `fk_progress_chapter` (`current_chapter_id`),
  KEY `fk_progress_scene` (`current_scene_id`),
  CONSTRAINT `fk_progress_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_progress_chapter` FOREIGN KEY (`current_chapter_id`) REFERENCES `story_chapters` (`id`),
  CONSTRAINT `fk_progress_scene` FOREIGN KEY (`current_scene_id`) REFERENCES `story_scenes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

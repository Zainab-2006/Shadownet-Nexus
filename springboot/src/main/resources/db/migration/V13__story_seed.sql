-- V13__story_seed.sql
-- Sample story content to prevent 500s on /api/story/*

INSERT IGNORE INTO story_chapters (chapter_number, title, description, is_locked, required_trust_level) VALUES
(1, 'Recruitment', 'NEXUS recruits you for ShadowNet takedown.', false, 0),
(2, 'Perimeter Breach', 'Infiltrate outer defenses.', true, 5),
(3, 'Core Infiltration', 'Reach the heart.', true, 20);

INSERT IGNORE INTO story_scenes (chapter_id, scene_number, content, scene_type, character_speaking, operator_pov_variants) VALUES
(1, 1, 'Welcome, Agent. ShadowNet is the target. Choose wisely.', 'CHOICE', 'NEXUS-AI', '{\"hacker\": \"Code ready.\", \"social\": \"Contacts active.\"}'),
(2, 1, 'Firewall down. Next?', 'NARRATIVE', 'Handler', '{}'),
(3, 1, 'Accuse?', 'CHOICE', 'Sable', '{}');

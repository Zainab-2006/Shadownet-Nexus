-- V21__story_content.sql
-- Bridge migration kept intentionally small. V24 owns the denser story seed.

INSERT INTO story_chapters (chapter_number, title, description, is_locked, required_trust_level) VALUES
(1, 'Recruitment', 'NEXUS recruits you. Choose path.', b'0', 0),
(2, 'Perimeter Breach', 'Outer defenses. Cyber or social?', b'1', 5),
(3, 'Core Infiltration', 'Heart of ShadowNet. Final choices.', b'1', 20)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  description = VALUES(description),
  required_trust_level = VALUES(required_trust_level);


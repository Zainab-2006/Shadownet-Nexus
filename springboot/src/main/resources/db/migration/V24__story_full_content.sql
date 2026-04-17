-- V24__story_full_content.sql
-- Playable story seed aligned with StoryScene's JPA element-collection choice schema.

INSERT INTO story_chapters (chapter_number, title, description, is_locked, required_trust_level) VALUES
(1, 'Recruitment', 'NEXUS Division recruits you into the fight against ShadowNet. Choose your entry path.', b'0', 0),
(2, 'Perimeter Breach', 'Outer defenses activated. Cyber infiltration or social engineering?', b'1', 5),
(3, 'Core Infiltration', 'Deep into ShadowNet core. Trust tested, final accusations.', b'1', 20)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  description = VALUES(description),
  is_locked = VALUES(is_locked),
  required_trust_level = VALUES(required_trust_level);

UPDATE story_scenes SET
  content = 'Agent, ShadowNet is destabilizing global infrastructure. Direct breach or social intel?',
  scene_type = 'CHOICE',
  character_speaking = 'NEXUS-AI',
  operator_pov_variants = '{"hacker":"Code paths lighting up","analyst":"Social graphs forming"}',
  next_scene_id = NULL
WHERE chapter_id = (SELECT id FROM story_chapters WHERE chapter_number = 1) AND scene_number = 1;

UPDATE story_scenes SET
  content = 'Perimeter IDS triggered. Choose the breach path.',
  scene_type = 'CHOICE',
  character_speaking = 'Perimeter AI',
  operator_pov_variants = '{"hacker":"Zero-days scanning","field":"Insider assets online"}',
  next_scene_id = NULL
WHERE chapter_id = (SELECT id FROM story_chapters WHERE chapter_number = 2) AND scene_number = 1;

UPDATE story_scenes SET
  content = 'Core access granted. Magnus signature confirmed. Resolve the final approach.',
  scene_type = 'CHOICE',
  character_speaking = 'Magnus',
  operator_pov_variants = '{"leader":"Alliance possible?","analyst":"Deception probability 87%"}',
  next_scene_id = NULL
WHERE chapter_id = (SELECT id FROM story_chapters WHERE chapter_number = 3) AND scene_number = 1;

INSERT INTO story_scenes (chapter_id, scene_number, content, scene_type, character_speaking, operator_pov_variants, next_scene_id)
SELECT c.id, 1, 'Agent, ShadowNet is destabilizing global infrastructure. Direct breach or social intel?', 'CHOICE', 'NEXUS-AI', '{"hacker":"Code paths lighting up","analyst":"Social graphs forming"}', NULL
FROM story_chapters c
WHERE c.chapter_number = 1
  AND NOT EXISTS (SELECT 1 FROM story_scenes s WHERE s.chapter_id = c.id AND s.scene_number = 1);

INSERT INTO story_scenes (chapter_id, scene_number, content, scene_type, character_speaking, operator_pov_variants, next_scene_id)
SELECT c.id, 2, 'Perimeter firewall engaged. Evidence fragment acquired.', 'NARRATIVE', 'Mission Control', '{}', NULL
FROM story_chapters c
WHERE c.chapter_number = 1
  AND NOT EXISTS (SELECT 1 FROM story_scenes s WHERE s.chapter_id = c.id AND s.scene_number = 2);

INSERT INTO story_scenes (chapter_id, scene_number, content, scene_type, character_speaking, operator_pov_variants, next_scene_id)
SELECT c.id, 3, 'First trust test passed. ShadowNet operator online. Confront or observe?', 'CHOICE', 'Draven Holt', '{"field":"Target acquired","analyst":"Behavioral baseline set"}', NULL
FROM story_chapters c
WHERE c.chapter_number = 1
  AND NOT EXISTS (SELECT 1 FROM story_scenes s WHERE s.chapter_id = c.id AND s.scene_number = 3);

INSERT INTO story_scenes (chapter_id, scene_number, content, scene_type, character_speaking, operator_pov_variants, next_scene_id)
SELECT c.id, 4, 'Recruitment complete. Chapter debrief: evidence logged, trust baseline established.', 'MISSION', 'NEXUS-AI', '{}', NULL
FROM story_chapters c
WHERE c.chapter_number = 1
  AND NOT EXISTS (SELECT 1 FROM story_scenes s WHERE s.chapter_id = c.id AND s.scene_number = 4);

INSERT INTO story_scenes (chapter_id, scene_number, content, scene_type, character_speaking, operator_pov_variants, next_scene_id)
SELECT c.id, 1, 'Perimeter IDS triggered. Choose the breach path.', 'CHOICE', 'Perimeter AI', '{"hacker":"Zero-days scanning","field":"Insider assets online"}', NULL
FROM story_chapters c
WHERE c.chapter_number = 2
  AND NOT EXISTS (SELECT 1 FROM story_scenes s WHERE s.chapter_id = c.id AND s.scene_number = 1);

INSERT INTO story_scenes (chapter_id, scene_number, content, scene_type, character_speaking, operator_pov_variants, next_scene_id)
SELECT c.id, 2, 'Breach established. Sable malware signature detected.', 'NARRATIVE', 'Elara Voss', '{}', NULL
FROM story_chapters c
WHERE c.chapter_number = 2
  AND NOT EXISTS (SELECT 1 FROM story_scenes s WHERE s.chapter_id = c.id AND s.scene_number = 2);

INSERT INTO story_scenes (chapter_id, scene_number, content, scene_type, character_speaking, operator_pov_variants, next_scene_id)
SELECT c.id, 3, 'Malware sample acquired. Dissect or deploy counter?', 'CHOICE', 'Sable', '{}', NULL
FROM story_chapters c
WHERE c.chapter_number = 2
  AND NOT EXISTS (SELECT 1 FROM story_scenes s WHERE s.chapter_id = c.id AND s.scene_number = 3);

INSERT INTO story_scenes (chapter_id, scene_number, content, scene_type, character_speaking, operator_pov_variants, next_scene_id)
SELECT c.id, 4, 'Perimeter secured. Trust elevated. Evidence chain strengthened.', 'MISSION', 'NEXUS-AI', '{}', NULL
FROM story_chapters c
WHERE c.chapter_number = 2
  AND NOT EXISTS (SELECT 1 FROM story_scenes s WHERE s.chapter_id = c.id AND s.scene_number = 4);

INSERT INTO story_scenes (chapter_id, scene_number, content, scene_type, character_speaking, operator_pov_variants, next_scene_id)
SELECT c.id, 1, 'Core access granted. Magnus signature confirmed. Resolve the final approach.', 'CHOICE', 'Magnus', '{"leader":"Alliance possible?","analyst":"Deception probability 87%"}', NULL
FROM story_chapters c
WHERE c.chapter_number = 3
  AND NOT EXISTS (SELECT 1 FROM story_scenes s WHERE s.chapter_id = c.id AND s.scene_number = 1);

INSERT INTO story_scenes (chapter_id, scene_number, content, scene_type, character_speaking, operator_pov_variants, next_scene_id)
SELECT c.id, 2, 'Core defenses crumbling. Final evidence synthesis.', 'NARRATIVE', 'NEXUS-AI', '{}', NULL
FROM story_chapters c
WHERE c.chapter_number = 3
  AND NOT EXISTS (SELECT 1 FROM story_scenes s WHERE s.chapter_id = c.id AND s.scene_number = 2);

INSERT INTO story_scenes (chapter_id, scene_number, content, scene_type, character_speaking, operator_pov_variants, next_scene_id)
SELECT c.id, 3, 'Accusation window open. Name the traitor.', 'CHOICE', 'Draven Holt', '{}', NULL
FROM story_chapters c
WHERE c.chapter_number = 3
  AND NOT EXISTS (SELECT 1 FROM story_scenes s WHERE s.chapter_id = c.id AND s.scene_number = 3);

INSERT INTO story_scenes (chapter_id, scene_number, content, scene_type, character_speaking, operator_pov_variants, next_scene_id)
SELECT c.id, 4, 'ShadowNet core neutralized. Full debrief unlocked.', 'MISSION', 'NEXUS Command', '{}', NULL
FROM story_chapters c
WHERE c.chapter_number = 3
  AND NOT EXISTS (SELECT 1 FROM story_scenes s WHERE s.chapter_id = c.id AND s.scene_number = 4);

UPDATE story_scenes s
JOIN story_scenes n ON n.chapter_id = s.chapter_id AND n.scene_number = s.scene_number + 1
SET s.next_scene_id = n.id
WHERE s.scene_number IN (1, 2, 3);

INSERT IGNORE INTO story_scene_choices (scene_id, id, text, trust_impact, next_scene_id) VALUES
((SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=1) AND scene_number=1 LIMIT 1), 1, 'Cyber Breach', 8, (SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=1) AND scene_number=2 LIMIT 1)),
((SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=1) AND scene_number=1 LIMIT 1), 2, 'Social Engineering', 3, (SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=1) AND scene_number=2 LIMIT 1)),
((SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=1) AND scene_number=1 LIMIT 1), 3, 'Hybrid Approach', 5, (SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=1) AND scene_number=2 LIMIT 1)),
((SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=1) AND scene_number=3 LIMIT 1), 1, 'Direct Confront', 10, (SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=1) AND scene_number=4 LIMIT 1)),
((SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=1) AND scene_number=3 LIMIT 1), 2, 'Passive Observe', 2, (SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=1) AND scene_number=4 LIMIT 1)),
((SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=1) AND scene_number=3 LIMIT 1), 3, 'Signal Jam', -5, (SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=1) AND scene_number=4 LIMIT 1)),
((SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=2) AND scene_number=1 LIMIT 1), 1, 'Zero-Day Exploit', 12, (SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=2) AND scene_number=2 LIMIT 1)),
((SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=2) AND scene_number=1 LIMIT 1), 2, 'Insider Creds', 6, (SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=2) AND scene_number=2 LIMIT 1)),
((SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=2) AND scene_number=1 LIMIT 1), 3, 'Brute Force', -8, (SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=2) AND scene_number=2 LIMIT 1)),
((SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=2) AND scene_number=3 LIMIT 1), 1, 'Dissect Malware', 14, (SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=2) AND scene_number=4 LIMIT 1)),
((SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=2) AND scene_number=3 LIMIT 1), 2, 'Deploy Countermeasure', 8, (SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=2) AND scene_number=4 LIMIT 1)),
((SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=2) AND scene_number=3 LIMIT 1), 3, 'Quarantine Sample', 5, (SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=2) AND scene_number=4 LIMIT 1)),
((SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=3) AND scene_number=1 LIMIT 1), 1, 'Full Assault', 25, (SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=3) AND scene_number=2 LIMIT 1)),
((SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=3) AND scene_number=1 LIMIT 1), 2, 'Negotiate', -15, (SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=3) AND scene_number=2 LIMIT 1)),
((SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=3) AND scene_number=1 LIMIT 1), 3, 'Extract Intel', 18, (SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=3) AND scene_number=2 LIMIT 1)),
((SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=3) AND scene_number=3 LIMIT 1), 1, 'Accuse Draven', 20, (SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=3) AND scene_number=4 LIMIT 1)),
((SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=3) AND scene_number=3 LIMIT 1), 2, 'Accuse Sable', -10, (SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=3) AND scene_number=4 LIMIT 1)),
((SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=3) AND scene_number=3 LIMIT 1), 3, 'Delay Accusation', 4, (SELECT id FROM story_scenes WHERE chapter_id=(SELECT id FROM story_chapters WHERE chapter_number=3) AND scene_number=4 LIMIT 1));

SELECT 'Chapters' as type, COUNT(*) as count FROM story_chapters WHERE chapter_number <= 3
UNION ALL SELECT 'Scenes', COUNT(*) FROM story_scenes WHERE chapter_id IN (SELECT id FROM story_chapters WHERE chapter_number <= 3)
UNION ALL SELECT 'Choices', COUNT(*) FROM story_scene_choices;


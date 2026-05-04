-- V24__story_full_content.sql
-- Portable story content migration for H2 and MySQL compatibility.

DELETE FROM story_chapters WHERE chapter_number IN (1, 2, 3);

INSERT INTO story_chapters (chapter_number, title, description, is_locked, required_trust_level) VALUES
  (1, 'Recruitment', 'NEXUS Division recruits you into the fight against ShadowNet. Choose your entry path.', 0, 0),
  (2, 'Perimeter Breach', 'Outer defenses activated. Cyber infiltration or social engineering?', 1, 5),
  (3, 'Core Infiltration', 'Deep into ShadowNet core. Trust tested, final accusations.', 1, 20);

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
JOIN (
  SELECT current_scene.id AS scene_id, next_scene.id AS next_scene_id
  FROM story_scenes current_scene
  LEFT JOIN story_scenes next_scene
    ON next_scene.chapter_id = current_scene.chapter_id
   AND next_scene.scene_number = current_scene.scene_number + 1
  WHERE current_scene.scene_number IN (1, 2, 3)
) scene_links
  ON scene_links.scene_id = s.id
SET s.next_scene_id = scene_links.next_scene_id;

DELETE FROM story_scene_choices
WHERE scene_id IN (
  SELECT id FROM story_scenes
  WHERE chapter_id IN (
    SELECT id FROM story_chapters WHERE chapter_number IN (1, 2, 3)
  )
);

INSERT INTO story_scene_choices (scene_id, id, text, trust_impact, next_scene_id)
SELECT s.id, 1, 'Cyber Breach', 8,
  (SELECT n.id FROM story_scenes n WHERE n.chapter_id = s.chapter_id AND n.scene_number = s.scene_number + 1)
FROM story_scenes s
JOIN story_chapters c ON c.id = s.chapter_id
WHERE c.chapter_number = 1 AND s.scene_number = 1;

INSERT INTO story_scene_choices (scene_id, id, text, trust_impact, next_scene_id)
SELECT s.id, 2, 'Social Engineering', 3,
  (SELECT n.id FROM story_scenes n WHERE n.chapter_id = s.chapter_id AND n.scene_number = s.scene_number + 1)
FROM story_scenes s
JOIN story_chapters c ON c.id = s.chapter_id
WHERE c.chapter_number = 1 AND s.scene_number = 1;

INSERT INTO story_scene_choices (scene_id, id, text, trust_impact, next_scene_id)
SELECT s.id, 3, 'Hybrid Approach', 5,
  (SELECT n.id FROM story_scenes n WHERE n.chapter_id = s.chapter_id AND n.scene_number = s.scene_number + 1)
FROM story_scenes s
JOIN story_chapters c ON c.id = s.chapter_id
WHERE c.chapter_number = 1 AND s.scene_number = 1;

INSERT INTO story_scene_choices (scene_id, id, text, trust_impact, next_scene_id)
SELECT s.id, 1, 'Direct Confront', 10,
  (SELECT n.id FROM story_scenes n WHERE n.chapter_id = s.chapter_id AND n.scene_number = s.scene_number + 1)
FROM story_scenes s
JOIN story_chapters c ON c.id = s.chapter_id
WHERE c.chapter_number = 1 AND s.scene_number = 3;

INSERT INTO story_scene_choices (scene_id, id, text, trust_impact, next_scene_id)
SELECT s.id, 2, 'Passive Observe', 2,
  (SELECT n.id FROM story_scenes n WHERE n.chapter_id = s.chapter_id AND n.scene_number = s.scene_number + 1)
FROM story_scenes s
JOIN story_chapters c ON c.id = s.chapter_id
WHERE c.chapter_number = 1 AND s.scene_number = 3;

INSERT INTO story_scene_choices (scene_id, id, text, trust_impact, next_scene_id)
SELECT s.id, 3, 'Signal Jam', -5,
  (SELECT n.id FROM story_scenes n WHERE n.chapter_id = s.chapter_id AND n.scene_number = s.scene_number + 1)
FROM story_scenes s
JOIN story_chapters c ON c.id = s.chapter_id
WHERE c.chapter_number = 1 AND s.scene_number = 3;

INSERT INTO story_scene_choices (scene_id, id, text, trust_impact, next_scene_id)
SELECT s.id, 1, 'Zero-Day Exploit', 12,
  (SELECT n.id FROM story_scenes n WHERE n.chapter_id = s.chapter_id AND n.scene_number = s.scene_number + 1)
FROM story_scenes s
JOIN story_chapters c ON c.id = s.chapter_id
WHERE c.chapter_number = 2 AND s.scene_number = 1;

INSERT INTO story_scene_choices (scene_id, id, text, trust_impact, next_scene_id)
SELECT s.id, 2, 'Insider Creds', 6,
  (SELECT n.id FROM story_scenes n WHERE n.chapter_id = s.chapter_id AND n.scene_number = s.scene_number + 1)
FROM story_scenes s
JOIN story_chapters c ON c.id = s.chapter_id
WHERE c.chapter_number = 2 AND s.scene_number = 1;

INSERT INTO story_scene_choices (scene_id, id, text, trust_impact, next_scene_id)
SELECT s.id, 3, 'Brute Force', -8,
  (SELECT n.id FROM story_scenes n WHERE n.chapter_id = s.chapter_id AND n.scene_number = s.scene_number + 1)
FROM story_scenes s
JOIN story_chapters c ON c.id = s.chapter_id
WHERE c.chapter_number = 2 AND s.scene_number = 1;

INSERT INTO story_scene_choices (scene_id, id, text, trust_impact, next_scene_id)
SELECT s.id, 1, 'Dissect Malware', 14,
  (SELECT n.id FROM story_scenes n WHERE n.chapter_id = s.chapter_id AND n.scene_number = s.scene_number + 1)
FROM story_scenes s
JOIN story_chapters c ON c.id = s.chapter_id
WHERE c.chapter_number = 2 AND s.scene_number = 3;

INSERT INTO story_scene_choices (scene_id, id, text, trust_impact, next_scene_id)
SELECT s.id, 2, 'Deploy Countermeasure', 8,
  (SELECT n.id FROM story_scenes n WHERE n.chapter_id = s.chapter_id AND n.scene_number = s.scene_number + 1)
FROM story_scenes s
JOIN story_chapters c ON c.id = s.chapter_id
WHERE c.chapter_number = 2 AND s.scene_number = 3;

INSERT INTO story_scene_choices (scene_id, id, text, trust_impact, next_scene_id)
SELECT s.id, 3, 'Quarantine Sample', 5,
  (SELECT n.id FROM story_scenes n WHERE n.chapter_id = s.chapter_id AND n.scene_number = s.scene_number + 1)
FROM story_scenes s
JOIN story_chapters c ON c.id = s.chapter_id
WHERE c.chapter_number = 2 AND s.scene_number = 3;

INSERT INTO story_scene_choices (scene_id, id, text, trust_impact, next_scene_id)
SELECT s.id, 1, 'Full Assault', 25,
  (SELECT n.id FROM story_scenes n WHERE n.chapter_id = s.chapter_id AND n.scene_number = s.scene_number + 1)
FROM story_scenes s
JOIN story_chapters c ON c.id = s.chapter_id
WHERE c.chapter_number = 3 AND s.scene_number = 1;

INSERT INTO story_scene_choices (scene_id, id, text, trust_impact, next_scene_id)
SELECT s.id, 2, 'Negotiate', -15,
  (SELECT n.id FROM story_scenes n WHERE n.chapter_id = s.chapter_id AND n.scene_number = s.scene_number + 1)
FROM story_scenes s
JOIN story_chapters c ON c.id = s.chapter_id
WHERE c.chapter_number = 3 AND s.scene_number = 1;

INSERT INTO story_scene_choices (scene_id, id, text, trust_impact, next_scene_id)
SELECT s.id, 3, 'Extract Intel', 18,
  (SELECT n.id FROM story_scenes n WHERE n.chapter_id = s.chapter_id AND n.scene_number = s.scene_number + 1)
FROM story_scenes s
JOIN story_chapters c ON c.id = s.chapter_id
WHERE c.chapter_number = 3 AND s.scene_number = 1;

INSERT INTO story_scene_choices (scene_id, id, text, trust_impact, next_scene_id)
SELECT s.id, 1, 'Accuse Draven', 20,
  (SELECT n.id FROM story_scenes n WHERE n.chapter_id = s.chapter_id AND n.scene_number = s.scene_number + 1)
FROM story_scenes s
JOIN story_chapters c ON c.id = s.chapter_id
WHERE c.chapter_number = 3 AND s.scene_number = 3;

INSERT INTO story_scene_choices (scene_id, id, text, trust_impact, next_scene_id)
SELECT s.id, 2, 'Accuse Sable', -10,
  (SELECT n.id FROM story_scenes n WHERE n.chapter_id = s.chapter_id AND n.scene_number = s.scene_number + 1)
FROM story_scenes s
JOIN story_chapters c ON c.id = s.chapter_id
WHERE c.chapter_number = 3 AND s.scene_number = 3;

INSERT INTO story_scene_choices (scene_id, id, text, trust_impact, next_scene_id)
SELECT s.id, 3, 'Delay Accusation', 4,
  (SELECT n.id FROM story_scenes n WHERE n.chapter_id = s.chapter_id AND n.scene_number = s.scene_number + 1)
FROM story_scenes s
JOIN story_chapters c ON c.id = s.chapter_id
WHERE c.chapter_number = 3 AND s.scene_number = 3;

SELECT 'Chapters' AS type, COUNT(*) AS count FROM story_chapters WHERE chapter_number <= 3
UNION ALL SELECT 'Scenes', COUNT(*) FROM story_scenes WHERE chapter_id IN (SELECT id FROM story_chapters WHERE chapter_number <= 3)
UNION ALL SELECT 'Choices', COUNT(*) FROM story_scene_choices;

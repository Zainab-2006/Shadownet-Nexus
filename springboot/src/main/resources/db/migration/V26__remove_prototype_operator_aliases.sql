-- V26__remove_prototype_operator_aliases.sql
-- Remove V1/DataSeeder prototype operator ids now that the locked 24-operator roster is canonical.

UPDATE users
SET selected_operator = CASE selected_operator
  WHEN 'op_analyst' THEN 'op_elara-voss'
  WHEN 'op_field' THEN 'op_marcus-webb'
  WHEN 'op_hacker' THEN 'op_ciphershade'
  ELSE selected_operator
END
WHERE selected_operator IN ('op_analyst', 'op_field', 'op_hacker');

INSERT INTO user_operators (user_id, operator_id, selected)
SELECT user_id,
  CASE operator_id
    WHEN 'op_analyst' THEN 'op_elara-voss'
    WHEN 'op_field' THEN 'op_marcus-webb'
    WHEN 'op_hacker' THEN 'op_ciphershade'
  END AS operator_id,
  MAX(selected) AS selected
FROM user_operators
WHERE operator_id IN ('op_analyst', 'op_field', 'op_hacker')
GROUP BY user_id, operator_id
ON DUPLICATE KEY UPDATE selected = GREATEST(selected, VALUES(selected));

DELETE FROM user_operators
WHERE operator_id IN ('op_analyst', 'op_field', 'op_hacker');

DELETE FROM operators
WHERE id IN ('op_analyst', 'op_field', 'op_hacker');
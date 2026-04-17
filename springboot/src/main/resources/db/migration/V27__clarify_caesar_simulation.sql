-- V27__clarify_caesar_simulation.sql
-- Make the first crypto simulation playable from the UI by giving the player the actual ciphertext and answer format.

UPDATE challenges
SET
  description = 'Decode the Caesar-rotated flag. Ciphertext: synt{pnrfne}. Submit the decoded value as flag{...}.',
  stages = '[{"briefing":"Recover the plaintext from this Caesar rotation: synt{pnrfne}.","objective":"Decode the Caesar-rotated flag.","evidence":"synt{pnrfne}","submitFormat":"flag{decoded_plaintext}","flagHash":"s5pBlsDtvk4vy8aBYmpaMNX3PjVoQahyp3FIN44oMtc=","learningContent":"Try all 25 non-zero rotations. When the ciphertext becomes a readable flag, submit it exactly in flag{...} format."}]',
  hints = '[{"content":"The ciphertext is synt{pnrfne}. It is a Caesar rotation."},{"content":"Try ROT13 on the ciphertext."},{"content":"The decoded answer keeps the flag{...} wrapper."}]',
  explanation = 'Caesar rotations shift each letter by a fixed amount. synt{pnrfne} under ROT13 becomes the accepted flag.'
WHERE id = 'crypto-001';

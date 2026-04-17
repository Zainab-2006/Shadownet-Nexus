-- V20__full_operator_seed.sql
-- Seed all 25 operators from frontend roster for backend truth

INSERT IGNORE INTO operators (id, name, role, abilities, unlock_cost, backstory) VALUES
('op_draven-holt', 'Draven Holt', 'Supreme Commander', '["Psychological Warfare", "Black Market Diplomacy"]', 0, 'Draven Holt directs large-scale theater operations.'),
('op_kira-vale', 'Kira Vale', 'Precision Assassin', '["Thermal Cloaking", "Zero-Sound Sniping"]', 0, 'Kira Vale is a surgical hunter built for silent elimination.'),
('op_dante-morgana', 'Dante Morgana', 'System Architect', '["Zero-Day Engineering", "Infrastructure Takeover"]', 0, 'Dante Morgana designs the battlefield before anyone enters it.'),
('op_elara-voss', 'Elara Voss', 'Cyber Intelligence Queen', '["Behavioral Threat Prediction", "Surveillance Logic"]', 0, 'Elara Voss fuses predictive analytics into mission-grade foresight.'),
('op_jax-storm', 'Jax Storm', 'Tactical Commander', '["Battlefield Simulation", "Electronic Warfare"]', 0, 'Jax Storm coordinates battlefield tempo.'),
('op_kai-chen', 'Kai Chen', 'Phantom Assassin', '["Silent Neutralization", "Zero-Footprint Infiltration"]', 0, 'Kai Chen erases presence and neutralizes targets.'),
('op_marcus-webb', 'Marcus Webb', 'Fortress Architect', '["Cyber Defense Architecture", "Threat Containment"]', 0, 'Marcus Webb builds defensive architecture under pressure.'),
('op_sable', 'Sable', 'Malware Architect', '["Polymorphic Malware", "Rootkit Engineering"]', 0, 'Sable writes living malicious code.'),
('op_valerie-cross', 'Valerie Cross', 'War Strategy Engine', '["Mission Probability Forecasting", "Risk Matrix"]', 0, 'Valerie Cross turns probability into combat logic.'),
('op_yuna-park', 'Yuna Park', 'Network Guardian', '["Secure Communication", "Signal Interception"]', 0, 'Yuna Park dominates communication layers.'),
('op_darren-kane', 'Darren Kane', 'Exploit Engineer', '["Memory Exploitation", "Kernel Attacks"]', 0, 'Darren Kane weaponizes memory and binaries.'),
('op_sloane-harper', 'Sloane Harper', 'Mission Logistics Master', '["Real-Time Tactical Routing", "Multi-Squad Coordination"]', 0, 'Sloane Harper keeps squads connected.'),
('op_echo-13', 'Echo-13', 'AI Combat Machine', '["Autonomous Combat", "Drone Swarm Control"]', 0, 'Echo-13 is a hardened autonomous combat chassis.'),
('op_magnus', 'Magnus', 'Supreme Villain', '["Global Chaos Engineering", "Strategic Deception"]', 0, 'Magnus rules through orchestrated chaos.'),
('op_dr-rowan', 'Dr. Rowan', 'Psycho-AI Engineer', '["Neural Hacking", "Mind-Control Code"]', 0, 'Dr. Rowan engineers attacks on cognition.'),
('op_ciphershade', 'CipherShade', 'Neural Cyber Assassin', '["Cybernetic Reflex", "Neural Kill Protocols"]', 0, 'CipherShade is a lethal cybernetic executioner.'),
('op_iris', 'Iris', 'Reality Distortion Specialist', '["Reality Distortion Tech", "Perception Hijacking"]', 0, 'Iris fractures perception itself.'),
('op_vex', 'Vex', 'Tactical Combat AI', '["Threat Interception", "Heavy Weapon Control"]', 0, 'Vex intercepts threats head-on.'),
('op_juno', 'Juno', 'Cyber Assassination Specialist', '["Cyber Assassination", "Ghost Payload"]', 0, 'Juno specializes in remote execution chains.'),
('op_kael', 'Kael', 'Infrastructure Annihilator', '["Logic Bomb Engineering", "System Meltdown"]', 0, 'Kael turns systems into demolition sites.'),
('op_maya-santos', 'Maya Santos', 'Reality System Corruptor', '["Reality System Corruption", "Neural Desync"]', 0, 'Maya destabilizes networks and minds.'),
('op_redline-ghost', 'Redline Ghost', 'Hyper-Speed Infiltrator', '["Hyper-Speed Infiltration", "Quantum Routing"]', 0, 'Redline Ghost moves faster than response systems.'),
('op_widowbyte', 'WidowByte', 'Silent Execution Malware', '["Silent Execution Malware", "Kill-Signal Programming"]', 0, 'WidowByte builds kill-signal traps.'),
('op_nullbyte', 'NullByte', 'Ransomware Architect', '["Self-Evolving Ransomware", "AI Encryption Loops"]', 0, 'NullByte engineers adaptive ransomware.');

-- Verify 25 ops seeded
SELECT COUNT(*) FROM operators;

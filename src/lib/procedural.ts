import { Challenge, Mission, TrustChoice, PCGChallenge } from '@/types';

/**
 * Procedural Content Generation System
 * Generates unique missions, challenges, and story content using algorithms
 */

/**
 * Simple seeded random number generator (no external dependency needed)
 */
class SeededRandom {
  private seed: number;

  constructor(seed: number) {
    this.seed = seed % 2147483647;
  }

  next(): number {
    this.seed = (this.seed * 16807) % 2147483647;
    return (this.seed - 1) / 2147483646;
  }
}

// Seeded random for reproducibility
const generateSeededRandom = (seed: number) => new SeededRandom(seed);

// Content templates for procedural generation
const MISSION_TYPES = [
  'corporate_espionage',
  'data_heist',
  'infrastructure_attack',
  'cyber_warfare',
] as const;

const CHALLENGE_CATEGORIES = [
  'web',
  'crypto',
  'pwn',
  'forensics',
  'rev',
  'osint',
  'misc',
] as const;

const WEB_CHALLENGES = [
  { title: 'SQL Injection Lab', base: 100 },
  { title: 'XSS Vulnerability', base: 150 },
  { title: 'CSRF Attack Simulation', base: 200 },
  { title: 'Authentication Bypass', base: 250 },
  { title: 'API Endpoint Exploitation', base: 300 },
];

const CRYPTO_CHALLENGES = [
  { title: 'Caesar Cipher Cracking', base: 50 },
  { title: 'RSA Factorization', base: 200 },
  { title: 'AES Decryption', base: 250 },
  { title: 'Hash Collision Finding', base: 300 },
  { title: 'Elliptic Curve Attacks', base: 400 },
];

const PWN_CHALLENGES = [
  { title: 'Stack Buffer Overflow', base: 150 },
  { title: 'Heap Exploitation', base: 350 },
  { title: 'Return-to-libc Attack', base: 300 },
  { title: 'ROP Gadget Chaining', base: 400 },
  { title: 'Format String Vulnerability', base: 250 },
];

const FORENSICS_CHALLENGES = [
  { title: 'Memory Dump Analysis', base: 200 },
  { title: 'Disk Image Investigation', base: 250 },
  { title: 'Malware Behavior Analysis', base: 300 },
  { title: 'Network Traffic Analysis', base: 200 },
  { title: 'File Carving Recovery', base: 250 },
];

const REV_CHALLENGES = [
  { title: 'Binary Decompilation', base: 200 },
  { title: 'Obfuscation Removal', base: 300 },
  { title: 'Algorithm Recovery', base: 250 },
  { title: 'Anti-Debugging Bypass', base: 350 },
  { title: 'Virtual Machine Analysis', base: 400 },
];

const OSINT_CHALLENGES = [
  { title: 'Username Enumeration', base: 100 },
  { title: 'Domain Discovery', base: 150 },
  { title: 'Social Engineering Intelligence', base: 200 },
  { title: 'Metadata Extraction', base: 150 },
  { title: 'Digital Footprint Mapping', base: 250 },
];

const MISC_CHALLENGES = [
  { title: 'Steganography Detection', base: 100 },
  { title: 'Puzzle Solving', base: 50 },
  { title: 'Logic Challenge', base: 150 },
  { title: 'Forensic Reconstruction', base: 200 },
];

const CHALLENGE_TEMPLATES: Record<string, Array<{ title: string; base: number }>> = {
  web: WEB_CHALLENGES,
  crypto: CRYPTO_CHALLENGES,
  pwn: PWN_CHALLENGES,
  forensics: FORENSICS_CHALLENGES,
  rev: REV_CHALLENGES,
  osint: OSINT_CHALLENGES,
  misc: MISC_CHALLENGES,
};

const DIFFICULTY_MULTIPLIERS: Record<string, number> = {
  easy: 0.5,
  medium: 1,
  hard: 1.5,
  insane: 2,
};

const MISSION_NAMES = {
  corporate_espionage: [
    'Corporate Breach',
    'Insider Job Investigation',
    'Executive Data Extraction',
    'Trade Secret Recovery',
    'Financial System Infiltration',
  ],
  data_heist: [
    'Database Theft',
    'Archive Extraction',
    'Secure Vault Cracking',
    'Encrypted Storage Assault',
    'Information Blackmail',
  ],
  infrastructure_attack: [
    'System Takeover',
    'Network Destruction',
    'Power Grid Assault',
    'Communication Disruption',
    'Critical Infrastructure Strike',
  ],
  cyber_warfare: [
    'Digital Battleground',
    'Malware Deployment',
    'Bot Network Control',
    'DDoS Orchestration',
    'Cyber Terrorism Prevention',
  ],
};

const CHOICE_TEMPLATES = [
  {
    text: 'Proceed cautiously with stealth approach',
    trustDelta: 10,
    outcome: 'success' as const,
  },
  {
    text: 'Rush in with aggressive tactics',
    trustDelta: -15,
    outcome: 'fail' as const,
  },
  {
    text: 'Use diplomacy and negotiation',
    trustDelta: 20,
    outcome: 'success' as const,
  },
  {
    text: 'Trust the team\'s instincts',
    trustDelta: 15,
    outcome: 'neutral' as const,
  },
  {
    text: 'Go solo - minimizes risk',
    trustDelta: -20,
    outcome: 'fail' as const,
  },
];

/**
 * Generate a unique challenge based on a seed and parameters
 */
export const generatePCGChallenge = (
  seed: number,
  category?: string,
  difficulty?: string
): PCGChallenge => {
  const rng = generateSeededRandom(seed);

  const cat = (category || CHALLENGE_CATEGORIES[Math.floor(rng.next() * CHALLENGE_CATEGORIES.length)]) as 'web' | 'crypto' | 'pwn' | 'forensics' | 'rev' | 'osint' | 'misc';
  const diff = (difficulty || (['easy', 'medium', 'hard', 'insane'] as const)[Math.floor(rng.next() * 4)]) as 'easy' | 'medium' | 'hard' | 'insane';

  const templates = CHALLENGE_TEMPLATES[cat] || CHALLENGE_TEMPLATES.misc;
  const template = templates[Math.floor(rng.next() * templates.length)];

  const basePoints = template.base;
  const multiplier = DIFFICULTY_MULTIPLIERS[diff];
  const points = Math.round(basePoints * multiplier);

  // Generate a unique challenge ID
  const id = `pcg_${cat}_${seed}`;

  // Create a procedurally generated flag
  const flagComponents = [
    template.title.toLowerCase().replace(/\s+/g, '_'),
    Math.floor(rng.next() * 10000)
      .toString(16)
      .toUpperCase(),
    seed.toString(16),
  ];
  const flag = `CTF{${flagComponents.join('_')}}`;

  return {
    id,
    name: `${template.title} (${diff.toUpperCase()})`,
    category: cat,
    difficulty: diff,
    points,
    description: `Difficulty: ${diff}. Solve this ${cat} challenge to earn ${points} points.`,
    flag,
    seed,
  };
};

/**
 * Generate multiple unique challenges for a CTF session
 */
export const generateCTFChallenges = (
  sessionSeed: number,
  count: number = 10
): Challenge[] => {
  const challenges: Challenge[] = [];
  const rng = generateSeededRandom(sessionSeed);

  for (let i = 0; i < count; i++) {
    const challengeSeed = sessionSeed + i * 1000;
    const pcgChallenge = generatePCGChallenge(challengeSeed);

    challenges.push({
      id: pcgChallenge.id,
      name: pcgChallenge.name,
      category: pcgChallenge.category,
      difficulty: pcgChallenge.difficulty,
      points: pcgChallenge.points,
      description: pcgChallenge.description,
      author: 'PCG Engine',
      solves: Math.floor(rng.next() * 500),
      solved: false,
    });
  }

  return challenges;
};

/**
 * Generate a unique mission with procedural objectives and choices
 */
export const generatePCGMission = (
  seed: number,
  chapterId: number,
  characterId: string
): Mission => {
  const rng = generateSeededRandom(seed);

  const missionType = MISSION_TYPES[Math.floor(rng.next() * MISSION_TYPES.length)];
  const difficulty =
    (['easy', 'medium', 'hard', 'extreme'] as const)[
      Math.min(3, Math.floor(rng.next() * 5))
    ];

  const missionNames = MISSION_NAMES[missionType];
  const name = missionNames[Math.floor(rng.next() * missionNames.length)];

  const baseXP = { easy: 500, medium: 1000, hard: 1500, extreme: 2500 };
  const timeBase = { easy: 30, medium: 60, hard: 90, extreme: 120 };

  // Generate 2-4 objectives
  const objectiveCount = 2 + Math.floor(rng.next() * 3);
  const objectives: string[] = [];
  const objectiveTemplates = [
    'Infiltrate the {{location}}',
    'Extract {{item}}',
    'Bypass {{security}}',
    'Analyze {{data}}',
    'Decrypt {{file}}',
    'Deploy {{payload}}',
    'Evade {{threat}}',
    'Establish {{connection}}',
  ];

  for (let i = 0; i < objectiveCount; i++) {
    const template =
      objectiveTemplates[Math.floor(rng.next() * objectiveTemplates.length)];
    const detail = ['security system', 'firewall', 'encryption', 'IDS'][
      Math.floor(rng.next() * 4)
    ];
    objectives.push(template.replace('{{location}}', detail));
  }

  return {
    id: `pcg_mission_${seed}`,
    chapterId,
    name,
    type: missionType,
    difficulty,
    description: `Primary Objective: ${name}\nDifficulty: ${difficulty.toUpperCase()}\nEstimated Time: ${timeBase[difficulty]} minutes`,
    objectives,
    timeLimitSeconds: timeBase[difficulty] * 60,
    xpReward: baseXP[difficulty],
    completed: false,
  };
};

/**
 * Generate trust-based choices for a mission
 */
export const generatePCGChoices = (
  seed: number,
  operatorId: string
): TrustChoice[] => {
  const rng = generateSeededRandom(seed);
  const choiceCount = 2 + Math.floor(rng.next() * 2); // 2-3 choices

  const choices: TrustChoice[] = [];

  for (let i = 0; i < choiceCount; i++) {
    const template =
      CHOICE_TEMPLATES[Math.floor(rng.next() * CHOICE_TEMPLATES.length)];
    const delta = Math.floor((rng.next() - 0.5) * 40); // -20 to +20

    choices.push({
      text: template.text,
      trustDelta: delta,
      outcome: Math.random() > 0.33 ? 'success' : 'fail',
    });
  }

  return choices;
};

/**
 * Generate infinite unique content stream
 */
export function* generateContentStream(
  sessionSeed: number,
  contentType: 'challenges' | 'missions' | 'choices'
) {
  let counter = 0;

  while (true) {
    const seed = sessionSeed + counter;

    switch (contentType) {
      case 'challenges':
        yield generatePCGChallenge(seed);
        break;
      case 'missions':
        yield generatePCGMission(seed, 1, 'player');
        break;
      case 'choices':
        yield generatePCGChoices(seed, 'kai');
        break;
    }

    counter += 1000;
  }
}

/**
 * Create a unique session ID for persistent content
 */
export const createGameSession = (): number => {
  return Math.floor(Date.now() / 1000) + Math.floor(Math.random() * 100000);
};

/**
 * Verify a PCG flag (simple validation)
 */
export const verifyPCGFlag = (userFlag: string, expectedFlag: string): boolean => {
  return userFlag.trim().toLowerCase() === expectedFlag.toLowerCase();
};

/**
 * Shared constants for Shadownet Nexus
 */

export const GAME_CHAPTERS = [
  { id: 1, title: 'First Contact' },
  { id: 2, title: 'Ghost Protocol' },
  { id: 3, title: 'Broken Cipher' },
  { id: 4, title: 'Critical Mass' },
  { id: 5, title: 'Counter-Strike' },
  { id: 6, title: 'Rising Phoenix' },
  { id: 7, title: 'Zero Hour' },
  { id: 8, title: 'Endgame' },
] as const;

export const GAME_MODES = ['solo', 'team', 'story', 'ctf'] as const;
export type GameMode = typeof GAME_MODES[number];

export const OPERATOR_FACTIONS = ['hero', 'villain'] as const;
export type OperatorFaction = typeof OPERATOR_FACTIONS[number];

export const API_ENDPOINTS = {
  BASE: '/api',
  CHALLENGES: '/api/challenges',
  GAME: '/api/game',
  LEADERBOARD: '/api/leaderboard',
  MISSIONS: '/api/missions',
  OPERATORS: '/api/operators',
  PUZZLES: '/api/puzzles',
  USERS: '/api/users',
} as const;

export const BACKEND_OPERATOR_PREFIX = 'op_';

export const MAX_TRUST_LEVEL = 100;
export const MIN_TRUST_LEVEL = 0;

export const STORY_EVIDENCE_SOURCES = ['chapter', 'scene', 'choice'] as const;


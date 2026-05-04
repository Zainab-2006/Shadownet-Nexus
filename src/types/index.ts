export interface GameplayState {
  selectedOperator: string | null;
  trust: Record<string, number>;
  missionProgress: Record<string, unknown>;
  ctfSolves: string[];
  score: number;
  globalLeaderboard: LeaderboardEntry[];
  onlineUsers: number;
}

export interface LeaderboardEntry {
  rank: number;
  username: string;
  score: number;
  level: number;
}

export type { Mission } from './mission';
export type { Operator as Character } from './operator';

export interface Challenge {
  id: string;
  name: string;
  category: string;
  difficulty: string;
  points: number;
  description: string;
  author?: string;
  solves?: number;
  solved?: boolean;
  isSolved?: boolean;
}

export interface TrustChoice {
  id?: string;
  text?: string;
  label?: string;
  trustDelta?: number;
  consequence?: string;
  outcome?: 'success' | 'fail' | 'neutral';
}

export interface PCGChallenge {
  id: string;
  title?: string;
  name?: string;
  description: string;
  difficulty?: string;
  category?: string;
  flag?: string;
  points?: number;
  seed?: number;
}

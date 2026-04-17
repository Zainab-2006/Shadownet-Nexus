export interface GameplayState {
  selectedOperator: string | null;
  trust: Record<string, number>;
  missionProgress: Record<string, any>;
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

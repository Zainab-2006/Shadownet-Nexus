export interface GameplayState {
  selectedOperator: string | null;
  trust: Record<string, number>;
  missionProgress: Record<string, any>;
  storyEvidence: StoryEvidence[];
  unlockedMissions: string[];
  recommendedMissions: string[];
  storyConsequenceFlags: string[];
  ctfSolves: string[];
  score: number;
  globalLeaderboard: LeaderboardEntry[];
  onlineUsers: number;
}

export interface StoryEvidence {
  id?: number;
  evidenceCode: string;
  title: string;
  summary?: string;
  sourceChapterId?: number;
  sourceSceneId?: number;
  sourceChoiceId?: number;
  operatorInterpretation?: string;
  missionRelevanceTag?: string;
  discoveredAt?: string;
  newlyDiscovered?: boolean;
}

export interface LeaderboardEntry {
  username: string;
  score: number;
  rank: number;
  level: number;
}


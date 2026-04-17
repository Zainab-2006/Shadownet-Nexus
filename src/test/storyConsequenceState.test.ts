import { describe, expect, it } from 'vitest';
import { buildServerGameplayState } from '@/context/GameContext';
import type { StoryProgress } from '@/api/storyApi';
import type { User, UserProgression } from '@/api/userApi';
import type { LeaderboardEntry } from '@/types/gameplay';

const user: User = {
  id: 'user-1',
  username: 'agent',
  displayName: 'Agent',
  email: 'agent@example.com',
  score: 120,
  xp: 400,
  level: 3,
  selectedOperator: 'op_analyst',
};

const progression: UserProgression = {
  userId: 'user-1',
  totalXp: 400,
  currentLevel: 3,
  totalPoints: 160,
  rankPoints: 160,
  challengesSolved: 2,
  missionsCompleted: 1,
  storyProgressPercent: 25,
  solvedChallengeIds: ['challenge-1'],
};

const storyProgress: StoryProgress = {
  userId: 'user-1',
  currentChapterId: 1,
  currentSceneId: 2,
  completedChapters: [],
  completionPercentage: 25,
};

const leaderboard: LeaderboardEntry[] = [
  { username: 'agent', score: 160, rank: 1, level: 3 },
];

describe('buildServerGameplayState', () => {
  it('builds gameplay state from backend snapshots without local consequence projection', () => {
    const state = buildServerGameplayState({
      user,
      progression,
      storyProgress,
      missionStates: [
        { missionId: 'mission-web', state: 'RECOMMENDED' },
        { missionId: 'mission-forensics', state: 'UNLOCKED' },
        { missionId: 'mission-runtime', state: 'ACTIVE' },
      ],
      leaderboard,
      selectedOperatorId: 'op_analyst',
      onlineUsers: 4,
    });

    expect(state.selectedOperator).toBe('op_analyst');
    expect(state.score).toBe(160);
    expect(state.ctfSolves).toEqual(['challenge-1']);
    expect(state.missionProgress.story).toEqual(storyProgress);
    expect(state.missionProgress.missions['mission-web'].state).toBe('RECOMMENDED');
    expect(state.recommendedMissions).toEqual(['mission-web']);
    expect(state.unlockedMissions).toEqual(['mission-forensics', 'mission-runtime']);
    expect(state.globalLeaderboard).toEqual(leaderboard);
    expect(state.onlineUsers).toBe(4);
  });

  it('does not invent trust, evidence, or consequence flags from local defaults', () => {
    const state = buildServerGameplayState({
      user,
      progression,
      storyProgress,
      missionStates: [],
      leaderboard: [],
      selectedOperatorId: null,
      onlineUsers: 0,
    });

    expect(state.selectedOperator).toBeNull();
    expect(state.trust).toEqual({});
    expect(state.storyEvidence).toEqual([]);
    expect(state.storyConsequenceFlags).toEqual([]);
    expect(state.unlockedMissions).toEqual([]);
    expect(state.recommendedMissions).toEqual([]);
  });
});
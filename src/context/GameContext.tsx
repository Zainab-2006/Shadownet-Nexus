import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useReducer,
  useRef,
  useState,
  type ReactNode,
} from 'react';
import { useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { apiFetch } from '@/lib/apiClient';

import type { GameplayState, LeaderboardEntry } from '@/types/gameplay';
import type { StoryProgress } from '@/api/storyApi';
import type { Operator } from '@/types/operator';
import type { UserProgression, User } from '@/api/userApi';
import { useSelectOperator } from '@/api/operatorApi';
import { roster } from '@/data/roster';

interface ContextState {
  user: User | null;
  selectedOperator: Operator | null;
  progression: UserProgression | null;
  isLoading: boolean;
  isInitializing: boolean;
  error: string | null;
  isAuthenticated: boolean;
  syncMode: 'initializing' | 'refreshingFromBackend' | 'ready' | 'degraded' | 'authExpired' | 'syncError';
}


type GameAction =
  | { type: 'LOAD_SERVER_SNAPSHOT'; payload: GameplayState }
  | { type: 'SELECT_OPERATOR_OPTIMISTIC'; payload: string | null }
  | { type: 'SET_ONLINE_USERS'; payload: number }
  | { type: 'RESET' };

interface GameContextType extends ContextState {
  gameState: GameplayState;
  state: GameplayState;
  dispatchGameAction: React.Dispatch<GameAction>;
  dispatch: React.Dispatch<GameAction>;
  selectOperator: (operatorId: string | number) => Promise<void>;
  refreshUserData: () => Promise<void>;
  refreshProgression: () => Promise<void>;
  clearError: () => void;
  isInitialized: boolean;
}

interface UserMissionState {
  missionId: string;
  state: string;
  reason?: string;
  sourceChapterId?: number | null;
  sourceSceneId?: number | null;
  sourceChoiceId?: number | null;
  updatedAt?: string | null;
}

const LEGACY_STORAGE_KEYS = [
  'shadownet_cache_user',
  'shadownet_cache_operator',
  'shadownet_cache_progression',
  'shadownet_cache_gameplay',
  'shadownet_game_state',
  'selectedOperator',
  'storyProgress',
  'missionState',
  'teamId',
  'trainingMode',
  'rankedEligible',
];

const defaultGameplayState: GameplayState = {
  selectedOperator: null,
  trust: {},
  missionProgress: {},
  storyEvidence: [],
  unlockedMissions: [],
  recommendedMissions: [],
  storyConsequenceFlags: [],
  ctfSolves: [],
  score: 0,
  globalLeaderboard: [],
  onlineUsers: 0,
};

const DEFAULT_STATS = {
  attack: 60,
  defense: 60,
  speed: 60,
  tech: 60,
} as const;

const LEGACY_OPERATOR_ALIASES: Record<string, string> = {
  op_analyst: 'op_elara-voss',
  op_field: 'op_marcus-webb',
  op_hacker: 'op_ciphershade',
};

const canonicalOperatorId = (id?: string | null) => LEGACY_OPERATOR_ALIASES[id ?? ''] || id || '';

const rosterPresentationByBackendId = new Map(
  roster.map((character) => [character.backendOperatorId || `op_${character.id}`, character])
);

const rosterPresentationByName = new Map(
  roster.map((character) => [character.name.toLowerCase(), character])
);

const buildProgression = (user: User | null): UserProgression | null => {
  if (!user) return null;
  return {
    userId: user.id,
    totalXp: user.xp ?? 0,
    currentLevel: user.level ?? 1,
    totalPoints: user.score ?? 0,
    rankPoints: user.score ?? 0,
    challengesSolved: 0,
    missionsCompleted: 0,
    storyProgressPercent: 0,
    solvedChallengeIds: [],
  };
};

const normalizeOperator = (raw: unknown): Operator => {
  const canonicalId = canonicalOperatorId(String(raw?.id ?? ''));
  const name = String(raw?.name ?? 'Unknown Operator').trim();
  const role = String(raw?.role ?? 'Operative').trim();
  const presentation =
    rosterPresentationByBackendId.get(canonicalId) ||
    rosterPresentationByName.get(name.toLowerCase()) ||
    null;
  const abilitySource = typeof raw?.abilities === 'string' ? raw.abilities : '[]';
  let skills: string[] = [];

  try {
    const parsed = JSON.parse(abilitySource);
    skills = Array.isArray(parsed) ? parsed.map((value) => String(value).trim()).filter(Boolean) : [];
  } catch {
  skills = abilitySource
      .replace(/[\\[\\]"\\]]/g, '')
      .split(/[,|]/)
      .map((value) => value.trim())
      .filter(Boolean);
  }

  return {
    id: canonicalId,
    name,
    codename: presentation?.codename || name,
    role: presentation?.role || role,
    faction: presentation?.faction === 'villain' ? 'Villains' : 'Heroes',
    tier: 'operative',
    alignment: presentation?.faction || 'hero',
    specialty: skills[0] || role,
    specialization: skills[0] || role,
    personality: 'Focused and adaptive under pressure.',
    abilityType: skills[0] || role,
    abilityValue: Number(raw?.unlockCost ?? 0),
    unlockCost: Number(raw?.unlockCost ?? 0),
    unlocked: Boolean(raw?.unlocked),
    selected: Boolean(raw?.selected),
    portraitUrl: raw?.portraitUrl,
    fullImageUrl: raw?.fullImageUrl,
    bio: presentation?.background || String(raw?.backstory ?? 'No briefing available yet.'),
    storyline: presentation?.background || String(raw?.backstory ?? 'No briefing available yet.'),
    skills: skills.length > 0 ? skills : presentation?.skills.map((skill) => skill.name) ?? [],
    stats: { ...DEFAULT_STATS },
    trust: 50,
  };
};

const fetchOperators = async (): Promise<Operator[]> => {
  const operators = await apiFetch<unknown[]>('/operators');
  return Array.isArray(operators) ? operators.map(normalizeOperator) : [];
};

const normalizeMissionStates = (states: UserMissionState[]) => {
  const unlocked = new Set<string>();
  const recommended = new Set<string>();
  const byMissionId: Record<string, UserMissionState> = {};

  states.forEach((state) => {
    if (!state?.missionId) return;
    byMissionId[state.missionId] = state;
    const normalizedState = String(state.state ?? '').toUpperCase();
    if (['UNLOCKED', 'ACTIVE', 'COMPLETED'].includes(normalizedState)) {
      unlocked.add(state.missionId);
    }
    if (normalizedState === 'RECOMMENDED') {
      recommended.add(state.missionId);
    }
  });

  return {
    byMissionId,
    unlockedMissions: Array.from(unlocked),
    recommendedMissions: Array.from(recommended),
  };
};

export const buildServerGameplayState = ({
  user,
  progression,
  storyProgress,
  missionStates,
  leaderboard,
  selectedOperatorId,
  onlineUsers,
}: {
  user: User | null;
  progression: UserProgression | null;
  storyProgress: StoryProgress | null;
  missionStates: UserMissionState[];
  leaderboard: LeaderboardEntry[];
  selectedOperatorId: string | null;
  onlineUsers: number;
}): GameplayState => {
  const missionSnapshot = normalizeMissionStates(missionStates);

  return {
    ...defaultGameplayState,
    selectedOperator: selectedOperatorId,
    score: progression?.totalPoints ?? user?.score ?? defaultGameplayState.score,
    ctfSolves: progression?.solvedChallengeIds ?? [],
    globalLeaderboard: leaderboard,
    onlineUsers,
    unlockedMissions: missionSnapshot.unlockedMissions,
    recommendedMissions: missionSnapshot.recommendedMissions,
    missionProgress: {
      story: storyProgress,
      missions: missionSnapshot.byMissionId,
    },
  };
};

const gameReducer = (state: GameplayState, action: GameAction): GameplayState => {
  switch (action.type) {
    case 'LOAD_SERVER_SNAPSHOT':
      return action.payload;

    case 'SELECT_OPERATOR_OPTIMISTIC':
      return {
        ...state,
        selectedOperator: action.payload,
      };

    case 'SET_ONLINE_USERS':
      return {
        ...state,
        onlineUsers: action.payload,
      };

    case 'RESET':
      return defaultGameplayState;


    default:
      return state;
  }
};

const GameContext = createContext<GameContextType | null>(null);

export const GameProvider = ({ children }: { children: ReactNode }) => {
  const queryClient = useQueryClient();
  const selectOperatorMutation = useSelectOperator();

  const [contextState, setContextState] = useState<ContextState>({
    user: null,
    selectedOperator: null,
    progression: null,
    isLoading: false,
    isInitializing: true,
    error: null,
    isAuthenticated: !!localStorage.getItem('token'),
    syncMode: 'initializing',
  });

  const [gameState, baseDispatch] = useReducer(gameReducer, defaultGameplayState);
  const initStartedRef = useRef(false);

  const clearLegacyGameplayCache = useCallback(() => {
    LEGACY_STORAGE_KEYS.forEach((key) => localStorage.removeItem(key));
  }, []);

const APP_STATE_VERSION = 'v3';

const loadUserData = useCallback(async () => {
    const token = localStorage.getItem('token');

    if (!token) {
      // Clear stale state on auth expiry - Strict order start
      localStorage.removeItem('shadownet_game_state');
      clearLegacyGameplayCache();
      setContextState({
        user: null,
        selectedOperator: null,
        progression: null,
        isLoading: false,
        isInitializing: false,
        error: null,
        isAuthenticated: false,
        syncMode: 'authExpired',
      });
      baseDispatch({ type: 'RESET' });
      return;
    }

    if (localStorage.getItem('shadownet_state_version') !== APP_STATE_VERSION) {
      clearLegacyGameplayCache();
      localStorage.setItem('shadownet_state_version', APP_STATE_VERSION);
    }

    setContextState((prev) => ({
      ...prev,
      isLoading: true,
      isAuthenticated: true,
      error: null,
      syncMode: 'refreshingFromBackend',
    }));

    try {
      // Strict sequential: 1. token -> 2. user -> 3. operator -> 4. story/mission
      const user = await apiFetch<User>('/users/me');
      const progressionResponse = await apiFetch<UserProgression>('/users/me/progress');
      const progression = progressionResponse ?? buildProgression(user);
      const selectedOperatorId = canonicalOperatorId(user.selectedOperator ?? null);

      const operators = await queryClient.fetchQuery({ queryKey: ['operators'], queryFn: fetchOperators });
      const selectedOperator = operators.find((operator) => String(operator.id) === selectedOperatorId) ?? null;

      const [leaderboard, storyProgress, missionStates] = await Promise.all([
        apiFetch<LeaderboardEntry[]>('/leaderboard'),
        apiFetch<StoryProgress>('/story/progress'),
        apiFetch<UserMissionState[]>('/missions/progress'),
      ]);

      // Backend priority cache
      queryClient.setQueryData(['user'], user);
      queryClient.setQueryData(['userProfile'], user);
      queryClient.setQueryData(['userProgress'], progression);
      queryClient.setQueryData(['storyProgress'], storyProgress);
      queryClient.setQueryData(['missionProgress'], missionStates);
      queryClient.setQueryData(['leaderboard'], leaderboard);

      baseDispatch({
        type: 'LOAD_SERVER_SNAPSHOT',
        payload: buildServerGameplayState({
          user,
          progression,
          storyProgress,
          missionStates,
          leaderboard,
          selectedOperatorId,
          onlineUsers: gameState.onlineUsers,
        }),
      });

      setContextState({
        user,
        selectedOperator,
        progression,
        isLoading: false,
        isInitializing: false,
        error: null,
        isAuthenticated: true,
        syncMode: 'ready',
      });
    } catch (error) {
      console.error('loadUserData v3 failed:', error);
      toast.error('Backend sync failed - using minimal validated state');
      setContextState((prev) => ({
        ...prev,
        isLoading: false,
        isInitializing: false,
        isAuthenticated: !!token,
        error: 'Backend unavailable. Core state stable.',
        syncMode: 'degraded',
      }));
    }
  }, [clearLegacyGameplayCache, gameState.onlineUsers, queryClient]);

  useEffect(() => {
    if (initStartedRef.current) return;
    initStartedRef.current = true;
    void loadUserData();
  }, [loadUserData]);

  const invalidateGameplayQueries = useCallback(async () => {
    await Promise.allSettled([
      queryClient.invalidateQueries({ queryKey: ['user'] }),
      queryClient.invalidateQueries({ queryKey: ['userProfile'] }),
      queryClient.invalidateQueries({ queryKey: ['selectedOperator'] }),
      queryClient.invalidateQueries({ queryKey: ['userProgress'] }),
      queryClient.invalidateQueries({ queryKey: ['leaderboard'] }),
      queryClient.invalidateQueries({ queryKey: ['operators'] }),
      queryClient.invalidateQueries({ queryKey: ['storyProgress'] }),
      queryClient.invalidateQueries({ queryKey: ['storyChapters'] }),
      queryClient.invalidateQueries({ queryKey: ['missionProgress'] }),
      queryClient.invalidateQueries({ queryKey: ['missions'] }),
    ]);
  }, [queryClient]);

  const refreshUserData = useCallback(async () => {
    await invalidateGameplayQueries();
    await loadUserData();
  }, [invalidateGameplayQueries, loadUserData]);

  const refreshProgression = useCallback(async () => {
    try {
      const [user, progressionResponse, leaderboard, storyProgress, missionStates] = await Promise.all([
        apiFetch<User>('/users/me'),
        apiFetch<UserProgression>('/users/me/progress').catch(() => null),
        apiFetch<LeaderboardEntry[]>('/leaderboard').catch(() => gameState.globalLeaderboard),
        apiFetch<StoryProgress>('/story/progress').catch(() => null),
        apiFetch<UserMissionState[]>('/missions/progress').catch(() => [] as UserMissionState[]),
      ]);
      const progression = progressionResponse ?? buildProgression(user);

      setContextState((prev) => ({
        ...prev,
        user,
        progression,
        error: null,
        syncMode: 'ready',
      }));

      baseDispatch({
        type: 'LOAD_SERVER_SNAPSHOT',
        payload: buildServerGameplayState({
          user,
          progression,
          storyProgress,
          missionStates,
          leaderboard,
          selectedOperatorId: canonicalOperatorId(user.selectedOperator ?? gameState.selectedOperator),
          onlineUsers: gameState.onlineUsers,
        }),
      });

      await invalidateGameplayQueries();
    } catch (error) {
      console.error('refreshProgression failed:', error);
      setContextState((prev) => ({
        ...prev,
        error: 'Failed to refresh progression.',
        syncMode: 'syncError',
      }));
    }
  }, [gameState.globalLeaderboard, gameState.onlineUsers, gameState.selectedOperator, invalidateGameplayQueries]);

  const selectOperator = useCallback(
    async (operatorId: string | number) => {
      const operatorIdString = String(operatorId);
      const previousOperatorId = gameState.selectedOperator;
      const operators = (queryClient.getQueryData(['operators']) as Operator[] | undefined) ?? [];
      const optimisticOperator = operators.find((op) => String(op.id) === operatorIdString) ?? null;

      baseDispatch({ type: 'SELECT_OPERATOR_OPTIMISTIC', payload: operatorIdString });
      if (optimisticOperator) {
        setContextState((prev) => ({
          ...prev,
          selectedOperator: optimisticOperator,
          error: null,
        }));
      }

      try {
        await selectOperatorMutation.mutateAsync(operatorIdString);
        await refreshUserData();
      } catch (error) {
        console.error('selectOperator failed:', error);
        baseDispatch({ type: 'SELECT_OPERATOR_OPTIMISTIC', payload: previousOperatorId });
        setContextState((prev) => ({
          ...prev,
          error: 'Failed to select operator.',
        }));
        await refreshUserData();
      }
    },
    [gameState.selectedOperator, queryClient, refreshUserData, selectOperatorMutation]
  );

  const clearError = useCallback(() => {
    setContextState((prev) => ({
      ...prev,
      error: null,
    }));
  }, []);

  const dispatchGameAction = useCallback((action: GameAction) => {
    baseDispatch(action);
  }, []);

  useEffect(() => {
    const handleAuthLogin = () => {
      baseDispatch({ type: 'RESET' });
      void loadUserData();
    };

    const handleAuthLogout = () => {
      clearLegacyGameplayCache();
      setContextState({
        user: null,
        selectedOperator: null,
        progression: null,
        isLoading: false,
        isInitializing: false,
        error: null,
        isAuthenticated: false,
        syncMode: 'authExpired',
      });
      baseDispatch({ type: 'RESET' });
    };

    const handleOperatorChanged = () => {
      void refreshUserData();
    };

    window.addEventListener('auth:login', handleAuthLogin);
    window.addEventListener('auth:logout', handleAuthLogout);
    window.addEventListener('operator:changed', handleOperatorChanged);

    return () => {
      window.removeEventListener('auth:login', handleAuthLogin);
      window.removeEventListener('auth:logout', handleAuthLogout);
      window.removeEventListener('operator:changed', handleOperatorChanged);
    };
  }, [clearLegacyGameplayCache, loadUserData, refreshUserData]);

  const value = useMemo<GameContextType>(
    () => ({
      ...contextState,
      gameState,
      state: gameState,
      dispatchGameAction,
      dispatch: dispatchGameAction,
      selectOperator,
      refreshUserData,
      refreshProgression,
      clearError,
      isInitialized: !contextState.isInitializing,
    }),
    [
      clearError,
      contextState,
      dispatchGameAction,
      gameState,
      refreshProgression,
      refreshUserData,
      selectOperator,
    ]
  );

  return <GameContext.Provider value={value}>{children}</GameContext.Provider>;
};

export const useGame = () => {
  const context = useContext(GameContext);
  if (!context) {
    throw new Error('useGame must be used within GameProvider');
  }
  return context;
};

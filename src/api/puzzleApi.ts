import { apiFetch } from '../lib/apiClient';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

export interface PuzzleStageView {
  briefing?: string;
  objective?: string;
  evidence?: string;
  submitFormat?: string;
  learningContent?: string;
}

export interface PuzzleChallenge {
  id: string;
  name: string;
  title?: string;
  category: string;
  difficulty: string;
  points: number;
  description: string;
  author?: string;
  hasDockerRuntime?: boolean;
  stages: PuzzleStageView[];
}

export interface PuzzleSession {
  id: string;
  currentStage: number;
  hintsUsed: number;
  challenge: PuzzleChallenge;
  completed: boolean;
}

export const getPuzzleSession = (challengeId: string): Promise<PuzzleSession> =>
  apiFetch(`/puzzle/session/${challengeId}`) as Promise<PuzzleSession>;

export interface SubmitStagePayload {
  sessionId: string;
  stageNumber: number;
  flag: string;
  trainingMode?: boolean;
  rankedEligible?: boolean;
  solutionRevealed?: boolean;
  narratorTriggered?: boolean;
}

export interface SubmitStageResponse {
  correct: boolean;
  message?: string;
  showExplanation?: boolean;
  awardedPoints?: number;
  ranked?: boolean;
  trainingMode?: boolean;
  rankedEligible?: boolean;
  nextStage?: number;
  duplicate?: boolean;
  stale?: boolean;
}

export const submitStage = (payload: SubmitStagePayload): Promise<SubmitStageResponse> =>
  apiFetch('/puzzle/submit', {
    method: 'POST',
    data: JSON.stringify(payload)
  }) as Promise<SubmitStageResponse>;

export const getHint = (sessionId: string): Promise<unknown> =>
  apiFetch('/puzzle/hint', {
    method: 'POST',
    data: JSON.stringify({ sessionId })
  });

export const usePuzzleSession = (challengeId: string) => {
  return useQuery({
    queryKey: ['puzzleSession', challengeId],
    queryFn: () => getPuzzleSession(challengeId),
    enabled: !!challengeId && !!localStorage.getItem('token'),
    retry: false,
  });
};

export const useSubmitStage = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: SubmitStagePayload) =>
      submitStage(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['puzzleSession'] });
    },
  });
};

export const useGetHint = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (sessionId: string) => getHint(sessionId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['puzzleSession'] });
    },
  });
};

import { useQuery } from '@tanstack/react-query';
import { apiFetch, apiPost } from '../lib/apiClient';

export interface Chapter {
  id: number;
  chapterNumber: number;
  title: string;
  description: string;
  isLocked?: boolean;
  locked?: boolean;
  requiredTrustLevel: number;
}

export interface Scene {
  id: number;
  chapterId: number;
  sceneNumber: number;
  sceneType: 'NARRATIVE' | 'DIALOGUE' | 'CHOICE' | 'MISSION';
  content: string;
  characterSpeaking: string | null;
  operatorPovVariants: Record<string, string>;
  choices?: Array<{
    id: string;
    text: string;
    trustImpact: number;
    nextSceneId: number | null;
  }>;
  nextSceneId: number | null;
}

export interface StoryProgress {
  userId: string;
  currentChapterId: number | null;
  currentSceneId: number | null;
  completedChapters: number[];
  completionPercentage: number;
}

export interface DecisionRequest {
  sceneId: number;
  choiceId: number;
  operatorId?: string | number;
}

export interface DecisionResponse {
  progress: StoryProgress;
  nextSceneId: number | null;
  nextChapterId: number | null;
  trustImpact: number;
  trustDelta?: number;
  updatedTrust?: number;
  targetEntity: string;
  evidenceGained?: Array<{
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
  }>;
  consequenceFlags?: string[];
  unlockedMissionIds?: string[];
  recommendedMissionIds?: string[];
  missionChanges?: Array<{
    missionId: string;
    state: 'UNLOCKED' | 'RECOMMENDED' | string;
    reason?: string;
    newlyChanged?: boolean;
  }>;
  consequenceSummary?: {
    summary?: string;
    playerConclusion?: string;
    nextOperationalRisk?: string;
  };
  operatorInterpretation?: {
    operatorId?: string;
    lens?: string;
    evidenceAngle?: string;
    missionEmphasis?: string;
  } | null;
}

export const storyApi = {
  getChapters: (): Promise<Chapter[]> => apiFetch<Chapter[]>('/story/chapters'),
  getChapter: (id: number): Promise<Chapter> => apiFetch(`/story/chapters/${id}`),
  getScene: (id: number): Promise<Scene> => apiFetch(`/story/scenes/${id}`),
  getFirstScene: (chapterId: number): Promise<Scene> => apiFetch(`/story/chapters/${chapterId}/first-scene`),
  getProgress: (): Promise<StoryProgress> => apiFetch<StoryProgress>('/story/progress'),
  makeDecision: (request: DecisionRequest): Promise<DecisionResponse> =>
    apiFetch('/story/decision', {
      method: 'POST',
      data: JSON.stringify(request),
    }),
  resetProgress: (): Promise<void> => apiPost('/story/progress/reset').then(() => undefined),
};

export const useGetChaptersQuery = () => useQuery({
  queryKey: ['storyChapters'],
  queryFn: storyApi.getChapters,
  enabled: !!localStorage.getItem('token'),
});

export const useGetProgressQuery = () => useQuery({
  queryKey: ['storyProgress'],
  queryFn: storyApi.getProgress,
  enabled: !!localStorage.getItem('token'),
});

export const useGetSceneQuery = (sceneId: number) => useQuery({
  queryKey: ['storyScene', sceneId],
  queryFn: () => storyApi.getScene(sceneId),
  enabled: !!sceneId && !!localStorage.getItem('token'),
});

export const useGetFirstSceneQuery = (chapterId: number, options?: { skip?: boolean }) => useQuery({
  queryKey: ['firstScene', chapterId],
  queryFn: () => storyApi.getFirstScene(chapterId),
  enabled: !!chapterId && !!localStorage.getItem('token') && !options?.skip,
});

import { useMutation, useQueryClient } from '@tanstack/react-query';

export const useMakeDecisionMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: storyApi.makeDecision,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['storyProgress'] });
      queryClient.invalidateQueries({ queryKey: ['storyScene'] });
    },
  });
};





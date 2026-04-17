import { apiFetch, apiPost } from '../lib/apiClient';
import { useQuery } from '@tanstack/react-query';

export interface Challenge {
  id: string;
  _id?: string;
  name: string;
  title: string;
  category: string;
  difficulty: string;
  points: number;
  value?: number;
  description: string;
  author: string;
  solves: number;
  solveCount?: number;
  isSolved: boolean;
  solved?: boolean;
  attachments?: unknown[];
  files?: unknown[];
  stages?: unknown;
}

export const useChallenges = () => {
  return useQuery({
    queryKey: ['challenges'],
    queryFn: () => apiFetch<Challenge[]>('/challenges'),
  });
};

export interface ChallengeSubmitResponse {
  correct: boolean;
  message: string;
  pointsEarned: number;
  educationalExplanation?: string;
}

import { useMutation, useQueryClient } from '@tanstack/react-query';

export const useSubmitFlag = () => {
  const queryClient = useQueryClient();
  return useMutation({
mutationFn: ({challengeId, flag}: {challengeId: string, flag: string}) => 
      apiPost('/submit-flag', {challengeId, flag}).then(res => res.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['challenges'] });
      queryClient.invalidateQueries({ queryKey: ['user'] });
    },
  });
};


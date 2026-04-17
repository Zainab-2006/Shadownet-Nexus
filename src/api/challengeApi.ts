import { apiFetch, apiPost } from '../lib/apiClient';
import { useQuery } from '@tanstack/react-query';

export interface Challenge {
  id: string;
  name: string;
  title: string;
  category: string;
  difficulty: string;
  points: number;
  description: string;
  author: string;
  solves: number;
  isSolved: boolean;
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


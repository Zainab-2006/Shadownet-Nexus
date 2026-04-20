import { apiFetch } from '@/lib/apiClient';

export interface PCGChallengeViewDTO {
  instanceKey: string;
  title: string;
  description: string;
  category: string;
  difficulty: string;
  points: number;
  artifactJson?: string;
  attemptCount: number;
  hintsUsed: number;
  status: string;
}

export interface PCGChallengeSubmitResponse {
  correct: boolean;
  message: string;
  pointsAwarded: number;
  status: string;
}

export const generateSoloPCG = (payload: {
  sessionId: string;
  category?: string;
  difficulty?: string;
}): Promise<PCGChallengeViewDTO> =>
  apiFetch<PCGChallengeViewDTO>('/pcg/solo/generate', {
    method: 'POST',
    data: payload,
  });

export const getSoloPCG = (instanceKey: string): Promise<PCGChallengeViewDTO> =>
  apiFetch<PCGChallengeViewDTO>(`/pcg/solo/${instanceKey}`);

export const submitSoloPCG = (payload: {
  instanceKey: string;
  submittedFlag: string;
}): Promise<PCGChallengeSubmitResponse> =>
  apiFetch<PCGChallengeSubmitResponse>('/pcg/solo/submit', {
    method: 'POST',
    data: payload,
  });

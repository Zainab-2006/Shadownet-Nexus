import { apiFetch } from '@/lib/apiClient';

export interface StoryChoiceOptionDTO {
  choiceKey: string;
  label: string;
}

export interface StoryViewDTO {
  instanceKey: string;
  operatorCode: string;
  chapterCode: string;
  sceneCode: string;
  title: string;
  sceneText: string;
  trustLevel: number;
  affinityLevel: number;
  choicesCount: number;
  status: string;
  choices: StoryChoiceOptionDTO[];
}

export interface StoryDecisionResponse {
  success: boolean;
  message: string;
  nextSceneCode: string;
  trustLevel: number;
  affinityLevel: number;
  status: string;
}

export const startStoryRuntimeInstance = (payload: {
  operatorCode: string;
  chapterCode?: string;
  sceneCode?: string;
}): Promise<StoryViewDTO> =>
  apiFetch<StoryViewDTO>('/story-runtime/start', {
    method: 'POST',
    data: payload,
  });

export const getStoryRuntimeInstance = (instanceKey: string): Promise<StoryViewDTO> =>
  apiFetch<StoryViewDTO>(`/story-runtime/${instanceKey}`);

export const submitStoryRuntimeChoice = (payload: {
  instanceKey: string;
  choiceKey: string;
  chosenOption: string;
}): Promise<StoryDecisionResponse> =>
  apiFetch<StoryDecisionResponse>('/story-runtime/choice', {
    method: 'POST',
    data: payload,
  });

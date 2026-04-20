import { apiFetch } from '@/lib/apiClient';

export interface MissionEvidenceViewDTO {
  evidenceKey: string;
  evidenceType: string;
  found: boolean;
  contentJson: string | null;
}

export interface MissionViewDTO {
  instanceKey: string;
  missionCode: string;
  status: string;
  phase: string;
  trustScore: number;
  suspicionScore: number;
  credits: number;
  decisionsCount: number;
  visibleEvidence: MissionEvidenceViewDTO[];
}

export interface MissionDecisionResponse {
  success: boolean;
  message: string;
  trustScore: number;
  suspicionScore: number;
  credits: number;
  phase: string;
  status: string;
}

export const startMissionRuntimeInstance = (payload: {
  missionCode: string;
  squadId?: string;
}): Promise<MissionViewDTO> =>
  apiFetch<MissionViewDTO>('/mission-runtime/start', {
    method: 'POST',
    data: payload,
  });

export const getMissionRuntimeInstance = (instanceKey: string): Promise<MissionViewDTO> =>
  apiFetch<MissionViewDTO>(`/mission-runtime/${instanceKey}`);

export const submitMissionRuntimeDecision = (payload: {
  instanceKey: string;
  decisionKey: string;
  chosenOption: string;
}): Promise<MissionDecisionResponse> =>
  apiFetch<MissionDecisionResponse>('/mission-runtime/decision', {
    method: 'POST',
    data: payload,
  });

export const claimMissionRuntimeEvidence = (payload: {
  instanceKey: string;
  evidenceKey: string;
}): Promise<MissionViewDTO> =>
  apiFetch<MissionViewDTO>('/mission-runtime/evidence', {
    method: 'POST',
    data: payload,
  });

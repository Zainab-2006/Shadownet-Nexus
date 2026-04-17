export interface Mission {
  id: string;
  name: string;
  title: string;
  type: string;
  missionType: string;
  difficulty: string;
  description: string;
  story: string;
  objectives: string[];
  timeLimitSeconds: number;
  xpReward: number;
  completed: boolean;
  createdAt?: number;
}


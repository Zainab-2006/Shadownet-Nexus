export interface OperatorStats {
  attack: number;
  defense: number;
  speed: number;
  tech: number;
}

export interface Operator {
  id: string;
  name: string;
  codename: string;
  role: string;
  faction: string;
  tier: string;
  alignment: 'hero' | 'villain';
  specialty: string;
  specialization?: string;
  personality: string;
  visualTheme?: string;
  abilityType?: string;
  abilityValue?: number;
  unlockCost: number;
  unlocked: boolean;
  selected: boolean;
  portraitUrl?: string;
  fullImageUrl?: string;
  bio: string;
  storyline?: string;
  skills: string[];
  stats: OperatorStats;
  trust?: number;
}

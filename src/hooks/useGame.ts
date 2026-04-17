import { useGame as useGameContext } from '../context/GameContext';
import type { Operator, UserProgression, User } from '../api/userApi';
import type { Operator as OpType } from '../api/operatorApi';

export type { Operator, UserProgression, User };

// Main hook
export const useGame = useGameContext;

// Operator helpers
export const useSelectedOperator = () => {
  const { selectedOperator } = useGame();
  return { 
    operator: selectedOperator as OpType | null, 
    isLoading: false // Managed by GameContext
  };
};

// Progression helpers
export const useUserProgression = () => {
  const { progression, refreshProgression } = useGame();
  return { 
    progression: progression as UserProgression | null, 
    refreshProgression,
    isLoading: false 
  };
};

// Auth helpers
export const useIsAuthenticated = () => {
  const { isAuthenticated } = useGame();
  return { isAuthenticated };
};

// Combined game state
export const useGameState = () => {
  const game = useGame();
  return {
    ...game,
    isReady: game.isInitialized && !game.isLoading,
  };
};


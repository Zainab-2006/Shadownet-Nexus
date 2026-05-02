import { useContext } from 'react';
import { AuthContext } from './AuthContext';

export interface AuthContextType {
  token: string | null;
  user: unknown;
  login: (token: string, user: unknown) => void;
  logout: () => void;
  isValidating: boolean;
}

export const useAuthentication = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuthentication must be used within AuthProvider');
  return context;
};

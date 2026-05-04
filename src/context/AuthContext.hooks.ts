import { useContext } from 'react';
import { AuthContext } from './AuthContext';
import type { User } from '@/api/userApi';

export interface AuthContextType {
  token: string | null;
  user: User | null;
  login: (token: string, user: User) => void;
  logout: () => void;
  isValidating: boolean;
}

export const useAuthentication = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuthentication must be used within AuthProvider');
  return context;
};

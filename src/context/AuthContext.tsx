import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiFetch } from '@/lib/apiClient';

interface AuthContextType {
  token: string | null;
  user: any;
  login: (token: string, user: any) => void;
  logout: () => void;
  isValidating: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<any>(null);

  const { data: validatedUser, isLoading: validating } = useQuery({
    queryKey: ['user'],
    queryFn: () => apiFetch('/users/me'),
    enabled: !!token,
    retry: false,
  });

  useEffect(() => {
    const savedToken = localStorage.getItem('token');
    if (savedToken) {
      setToken(savedToken);
    }
  }, []);

  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    if (savedUser && savedUser !== 'null' && savedUser !== 'undefined') {
      try {
        setUser(JSON.parse(savedUser));
      } catch (e) {
        localStorage.removeItem('user');
        setUser(null);
      }
    } else {
      localStorage.removeItem('user');
    }
  }, []);

  useEffect(() => {
    if (validatedUser !== undefined && validatedUser !== null) {
      setUser(validatedUser);
      localStorage.setItem('user', JSON.stringify(validatedUser));
    } else if (token && validatedUser === null) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('refreshToken');
      setToken(null);
      setUser(null);
      window.dispatchEvent(new CustomEvent('auth:logout'));
    }
  }, [validatedUser, token]);

  const login = (newToken: string, newUser: any) => {
    localStorage.setItem('token', newToken);
    localStorage.setItem('user', JSON.stringify(newUser));
    setToken(newToken);
    setUser(newUser);
    window.dispatchEvent(new CustomEvent('auth:login'));
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('refreshToken');
    setToken(null);
    setUser(null);
    window.dispatchEvent(new CustomEvent('auth:logout'));
  };

  return (
    <AuthContext.Provider value={{ token, user, login, logout, isValidating: validating }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuthentication = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuthentication must be used within AuthProvider');
  return context;
};

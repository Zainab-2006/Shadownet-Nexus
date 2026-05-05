import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiFetch } from '../lib/apiClient';
import type { User } from './userApi';
import { prepareMutationContext } from './queryUtils';

export interface AuthResponse {
  token?: string;
  user?: User;
  message?: string;
}

type AuthAction = 'login' | 'register';

const AUTH_ATTEMPT_LIMIT = 5;
const AUTH_ATTEMPT_WINDOW_MS = 60_000;
const authAttempts = new Map<AuthAction, number[]>();

const assertClientAuthRateLimit = (action: AuthAction) => {
  const now = Date.now();
  const recentAttempts = (authAttempts.get(action) || []).filter(
    (attemptAt) => now - attemptAt < AUTH_ATTEMPT_WINDOW_MS,
  );

  if (recentAttempts.length >= AUTH_ATTEMPT_LIMIT) {
    throw new Error('Too many authentication attempts. Please wait a minute and try again.');
  }

  recentAttempts.push(now);
  authAttempts.set(action, recentAttempts);
};

const postAuth = <TResponse>(
  action: AuthAction,
  payload: Record<string, string>,
): Promise<TResponse> => {
  assertClientAuthRateLimit(action);
  return apiFetch<TResponse>(`/${action}`, {
    method: 'POST',
    data: payload,
  });
};

export const useLogin = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['login'],
    mutationFn: ({ email, password }: { email: string; password: string }) =>
      postAuth<AuthResponse>('login', { email, password }),
    onMutate: () => prepareMutationContext(queryClient, ['login'], 'login'),
  });
};

export const useRegister = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['register'],
    mutationFn: ({ email, username, password }: { email: string; username: string; password: string }) =>
      postAuth<AuthResponse>('register', { email, username, password }),
    onMutate: () => prepareMutationContext(queryClient, ['register'], 'register'),
  });
};

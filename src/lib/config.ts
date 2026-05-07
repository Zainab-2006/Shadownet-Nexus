// Defaults are same-origin relative paths. Absolute backend URLs are normalized
// so frontend calls like /challenges still resolve to Spring's /api/challenges.
const trimTrailingSlashes = (value: string): string => value.replace(/\/+$/, '');

const normalizeApiBase = (value?: string): string => {
  const rawValue = trimTrailingSlashes(value?.trim() || '/api');
  if (rawValue === '') {
    return '/api';
  }

  if (rawValue === '/api' || rawValue.endsWith('/api')) {
    return rawValue;
  }

  return `${rawValue}/api`;
};

const normalizeWsBase = (value?: string): string => {
  const rawValue = trimTrailingSlashes(value?.trim() || '');
  return rawValue.endsWith('/ws') ? rawValue.slice(0, -3) : rawValue;
};

export const API_BASE = normalizeApiBase(import.meta.env?.VITE_API_BASE_URL);
export const WS_BASE = normalizeWsBase(import.meta.env?.VITE_WS_BASE_URL);

export const ENV = {
  api: API_BASE,
  ws: WS_BASE,
  dev: !!import.meta.env?.DEV,
} as const;



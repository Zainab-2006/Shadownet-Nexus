const DEFAULT_API_ORIGIN = 'https://shadownet-nexus-mecf.onrender.com';

const normalizeApiOrigin = (value?: string): string => {
  if (!value) {
    return DEFAULT_API_ORIGIN;
  }

  return value.replace(/\/+$/, '').replace(/\/api$/, '');
};

const normalizeWsBase = (value?: string): string => {
  if (!value) {
    return '';
  }

  return value.replace(/\/+$/, '').replace(/\/ws$/, '');
};

const APP_BASE_URL = import.meta.env.DEV && !import.meta.env.VITE_API_BASE_URL
  ? ''
  : normalizeApiOrigin(import.meta.env.VITE_API_BASE_URL);

export const API_BASE = APP_BASE_URL ? `${APP_BASE_URL}/api` : '/api';
export const WS_BASE = normalizeWsBase(
  import.meta.env.VITE_WS_BASE_URL || import.meta.env.VITE_WS_URL || APP_BASE_URL,
);

export const ENV = {
  api: API_BASE,
  ws: WS_BASE,
  dev: import.meta.env.DEV,
} as const;

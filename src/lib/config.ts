export const API_BASE = '/api';
export const WS_BASE = '';

export const ENV = {
  api: API_BASE,
  ws: WS_BASE,
  dev: import.meta.env.DEV,
} as const;

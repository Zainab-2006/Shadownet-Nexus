export const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:3001/api';
export const WS_BASE = import.meta.env.VITE_WS_URL || 'http://localhost:3001';

export const ENV = {
  api: API_BASE,
  ws: WS_BASE,
  dev: import.meta.env.DEV,
} as const;

const APP_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'https://shadownet-nexus-mecf.onrender.com';

export const API_BASE = `${APP_BASE_URL}/api`;
export const WS_BASE = import.meta.env.VITE_WS_URL || APP_BASE_URL;

export const ENV = {
  api: API_BASE,
  ws: WS_BASE,
  dev: import.meta.env.DEV,
} as const;

// Defaults are same-origin relative paths.
// In some deployments (e.g., Vercel rewrite -> Render), you may want absolute URLs.
export const API_BASE = (import.meta as any).env?.VITE_API_BASE_URL ?? '/api';
export const WS_BASE = (import.meta as any).env?.VITE_WS_BASE_URL ?? '';

export const ENV = {
  api: API_BASE,
  ws: WS_BASE,
  dev: !!(import.meta as any).env?.DEV,
} as const;



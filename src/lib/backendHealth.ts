import { API_BASE } from './config';

type HealthResponse = {
  status?: string;
};

const sleep = (ms: number): Promise<void> => new Promise((resolve) => setTimeout(resolve, ms));

const healthUrl = (): string => {
  if (API_BASE === '/api') {
    return '/health';
  }

  if (API_BASE.endsWith('/api')) {
    return `${API_BASE.slice(0, -4)}/health`;
  }

  return `${API_BASE.replace(/\/+$/, '')}/health`;
};

let ready = false;
let readinessPromise: Promise<void> | null = null;

const fetchHealth = async (): Promise<boolean> => {
  const response = await fetch(healthUrl(), {
    cache: 'no-store',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  });

  if (!response.ok) {
    return false;
  }

  const data = (await response.json().catch(() => null)) as HealthResponse | null;
  return data?.status === 'ok';
};

export const waitForBackendReady = async (timeoutMs = 600_000): Promise<void> => {
  if (ready) {
    return;
  }

  if (readinessPromise) {
    return readinessPromise;
  }

  readinessPromise = (async () => {
    const startedAt = Date.now();
    let delayMs = 600;

    while (Date.now() - startedAt < timeoutMs) {
      try {
        if (await fetchHealth()) {
          ready = true;
          return;
        }
      } catch {
        // Render can briefly return 503 or close connections while a free instance wakes up.
      }

      await sleep(delayMs);
      delayMs = Math.min(Math.floor(delayMs * 1.4), 5_000);
    }

    readinessPromise = null;
    throw new Error('Backend is still starting. Please retry shortly.');
  })();

  return readinessPromise;
};

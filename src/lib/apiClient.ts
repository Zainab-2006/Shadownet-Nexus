import axios, {
  AxiosInstance,
  AxiosRequestConfig,
  AxiosResponse,
  AxiosError,
  InternalAxiosRequestConfig,
} from 'axios';
import { API_BASE } from './config';

const stripApiPrefix = (pathname: string): string => {
  if (pathname === '/api') {
    return '/';
  }

  return pathname.startsWith('/api/') ? pathname.slice(4) : pathname;
};

const normalizeApiPath = (url: string): string => {
  try {
    const parsedUrl = new URL(url, 'http://localhost');
    const normalizedPath = stripApiPrefix(parsedUrl.pathname);
    return `${normalizedPath}${parsedUrl.search}${parsedUrl.hash}`;
  } catch {
    return stripApiPrefix(url);
  }
};

type RequestConfigWithBody = AxiosRequestConfig & {
  body?: unknown;
  skipAuth?: boolean;
};

type InternalRequestConfig = InternalAxiosRequestConfig & {
  skipAuth?: boolean;
};

type RetryableAxiosError = AxiosError & {
  retryAfter?: string;
  isServiceUnavailable?: boolean;
  retryable503?: boolean;
};

const retryAfterHeader = (error: AxiosError): string | undefined => {
  const value = error.response?.headers?.['retry-after'];
  return Array.isArray(value) ? value[0] : value;
};

const toAxiosConfig = (url: string, config: RequestConfigWithBody = {}): RequestConfigWithBody => {
  const { body, ...restConfig } = config;
  const nextConfig: RequestConfigWithBody = {
    ...restConfig,
    url: normalizeApiPath(url),
  };

  if (body !== undefined && typeof nextConfig.data === 'undefined') {
    nextConfig.data = body;
  }

  return nextConfig;
};

class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE,
      withCredentials: true,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.client.interceptors.request.use(
      (config: InternalAxiosRequestConfig) => {
        const requestConfig = config as InternalRequestConfig;
        const token = localStorage.getItem('token');
        if (token && !requestConfig.skipAuth) {
          requestConfig.headers.set('Authorization', `Bearer ${token}`);
        }
        return requestConfig;
      },
      (error: AxiosError) => Promise.reject(error),
    );

    this.client.interceptors.response.use(
      (response: AxiosResponse) => response,
      (error: AxiosError) => {
        const skipAuth = (error.config as RequestConfigWithBody | undefined)?.skipAuth === true;

        if ((error.response?.status === 401 || error.response?.status === 403) && !skipAuth) {
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          localStorage.removeItem('refreshToken');
          window.dispatchEvent(new CustomEvent('auth:logout'));
          if (window.location.pathname !== '/login') {
            window.location.href = '/login';
          }
        }

        // Mark 503s with a flag so React Query can decide whether/how to retry.
        // (We only add metadata; we don't change behavior here to keep changes minimal.)
        if (error.response?.status === 503) {
          const method = (error.config as AxiosRequestConfig | undefined)?.method?.toUpperCase?.() ?? '';
          const retryableError = error as RetryableAxiosError;
          retryableError.retryAfter = retryAfterHeader(error);
          retryableError.isServiceUnavailable = true;
          retryableError.retryable503 = method === 'GET';
        }


        return Promise.reject(error);
      },
    );

  }

  request<T = unknown>(url: string, config?: RequestConfigWithBody): Promise<AxiosResponse<T>> {
    return this.client.request<T>(toAxiosConfig(url, config));
  }

  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.client.post<T>(normalizeApiPath(url), data, config);
  }
}

export const apiClient = new ApiClient();
export const apiPost = <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> => apiClient.post<T>(url, data, config);

export const apiFetch = async <T = unknown>(url: string, config?: RequestConfigWithBody): Promise<T> => {
  const response = await apiClient.request<T>(url, config);
  return response.data;
};

/**
 * Helper for limited exponential backoff when backend returns 503.
 * Use this in queryFn/methods that should be GET+idempotent.
 */
export const apiFetchWithBackoff = async <T = unknown>(
  url: string,
  config: RequestConfigWithBody = {},
  opts: { attempts?: number; baseDelayMs?: number } = {},
): Promise<T> => {
  const attempts = opts.attempts ?? 3;
  const baseDelayMs = opts.baseDelayMs ?? 500;

  let lastErr: unknown;
  for (let i = 0; i < attempts; i++) {
    try {
      return await apiFetch<T>(url, config);
    } catch (err) {
      lastErr = err;
      const axiosError = axios.isAxiosError(err) ? err : undefined;
      const status = axiosError?.response?.status;
      const method = ((config?.method ?? 'GET') as string).toUpperCase();

      if (!(status === 503 && method === 'GET')) throw err;
      if (i === attempts - 1) throw err;

      const retryAfterRaw = axiosError ? retryAfterHeader(axiosError) : undefined;
      const retryAfterMs = retryAfterRaw ? Number(retryAfterRaw) * 1000 : 0;

      const expDelay = baseDelayMs * Math.pow(2, i);
      const jitter = Math.floor(Math.random() * 150);
      const delayMs = retryAfterMs > 0 ? retryAfterMs + jitter : expDelay + jitter;

      await new Promise((r) => setTimeout(r, delayMs));
    }
  }

  throw lastErr;
};

export default apiClient;


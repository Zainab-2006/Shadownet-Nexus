import axios, {
  AxiosInstance,
  AxiosRequestConfig,
  AxiosResponse,
  AxiosError,
  InternalAxiosRequestConfig,
} from 'axios';
import { API_BASE } from './config';

const PUBLIC_AUTH_PATHS = new Set([
  '/login',
  '/register',
  '/request-password-reset',
  '/reset-password',
  '/verify-email',
]);

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

const getNormalizedPathname = (url?: string): string | undefined => {
  if (!url) {
    return undefined;
  }

  try {
    return stripApiPrefix(new URL(url, 'http://localhost').pathname);
  } catch {
    return stripApiPrefix(url.split('?')[0].split('#')[0]);
  }
};

const isPublicAuthPath = (url?: string): boolean => {
  const pathname = getNormalizedPathname(url);
  return pathname ? PUBLIC_AUTH_PATHS.has(pathname) : false;
};

type RequestConfigWithBody = AxiosRequestConfig & { body?: unknown };

const toAxiosConfig = (url: string, config?: RequestConfigWithBody): AxiosRequestConfig => {
  const { body, ...restConfig } = config || {};
  const nextConfig: AxiosRequestConfig = {
    ...restConfig,
    url: normalizeApiPath(url),
  };

  if (body !== undefined && typeof nextConfig.data === 'undefined') {
    if (typeof body === 'string') {
      try {
        nextConfig.data = JSON.parse(body);
      } catch {
        nextConfig.data = body;
      }
    } else {
      nextConfig.data = body;
    }
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
        const token = localStorage.getItem('token');
        if (token && !isPublicAuthPath(config.url)) {
          config.headers.set('Authorization', `Bearer ${token}`);
        }
        return config;
      },
      (error: AxiosError) => Promise.reject(error),
    );

    this.client.interceptors.response.use(
      (response: AxiosResponse) => response,
      (error: AxiosError) => {
        const requestUrl = typeof error.config?.url === 'string' ? error.config.url : undefined;
        const authRequest = isPublicAuthPath(requestUrl);

        if ((error.response?.status === 401 || error.response?.status === 403) && !authRequest) {
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          localStorage.removeItem('refreshToken');
          window.dispatchEvent(new CustomEvent('auth:logout'));
          if (window.location.pathname !== '/login') {
            window.location.href = '/login';
          }
        }
        return Promise.reject(error);
      },
    );
  }

  request<T = unknown>(url: string, config?: RequestConfigWithBody): Promise<AxiosResponse<T>> {
    return this.client.request<T>(toAxiosConfig(url, config));
  }

  get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.client.get<T>(normalizeApiPath(url), config);
  }

  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.client.post<T>(normalizeApiPath(url), data, config);
  }

  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.client.put<T>(normalizeApiPath(url), data, config);
  }

  delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.client.delete<T>(normalizeApiPath(url), config);
  }
}

export const apiClient = new ApiClient();
export const apiGet = <T = unknown>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> => apiClient.get<T>(url, config);
export const apiPost = <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> => apiClient.post<T>(url, data, config);
export const apiPut = <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> => apiClient.put<T>(url, data, config);
export const apiDelete = <T = unknown>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> => apiClient.delete<T>(url, config);

export const apiFetch = async <T = unknown>(url: string, config?: RequestConfigWithBody): Promise<T> => {
  const response = await apiClient.request<T>(url, config);
  return response.data;
};

export default apiClient;

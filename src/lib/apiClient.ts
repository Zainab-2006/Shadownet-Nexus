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

export default apiClient;

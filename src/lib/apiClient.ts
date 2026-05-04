import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError, InternalAxiosRequestConfig } from 'axios';
import { API_BASE } from './config';

const normalizeApiPath = (url: string): string => {
  if (url.startsWith('/api/')) {
    return url.slice(4);
  }
  return url;
};

const isPublicAuthPath = (url?: string): boolean => {
  if (!url) {
    return false;
  }

  const normalized = normalizeApiPath(url);
  return normalized === '/login'
    || normalized === '/register'
    || normalized === '/request-password-reset'
    || normalized === '/reset-password'
    || normalized === '/verify-email';
};

type RequestConfigWithBody = AxiosRequestConfig & { body?: unknown };

const toAxiosConfig = (url: string, config?: RequestConfigWithBody): AxiosRequestConfig => {
  const nextConfig: AxiosRequestConfig = {
    ...config,
    url: normalizeApiPath(url),
  };

  if ('body' in (config || {}) && config?.body !== undefined && typeof nextConfig.data === 'undefined') {
    const rawBody = config.body as unknown;
    if (typeof rawBody === 'string') {
      try {
        nextConfig.data = JSON.parse(rawBody);
      } catch (e) {
        nextConfig.data = rawBody;
      }
    } else {
      nextConfig.data = rawBody;
    }
    const configWithBody = nextConfig as AxiosRequestConfig & { body?: unknown };
    delete configWithBody.body;
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

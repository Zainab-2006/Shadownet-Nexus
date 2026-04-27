import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'https://shadownet-nexus-mecf.onrender.com';

const normalizeApiPath = (url: string): string => {
  if (url.startsWith('/api/')) {
    return url.slice(4);
  }
  return url;
};

const toAxiosConfig = (url: string, config?: AxiosRequestConfig): AxiosRequestConfig => {
  const nextConfig: AxiosRequestConfig = {
    ...config,
    url: normalizeApiPath(url),
  };

  if ('body' in (config || {}) && config.body !== undefined && typeof nextConfig.data === 'undefined') {
    const rawBody = config.body as unknown;
    if (typeof rawBody === 'string') {
      try {
        nextConfig.data = JSON.parse(rawBody);
      } catch {
        nextConfig.data = rawBody;
      }
    } else {
      nextConfig.data = rawBody;
    }
// eslint-disable-next-line @typescript-eslint/no-explicit-any
delete (nextConfig as any).body;
  }

  return nextConfig;
};

class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      withCredentials: true,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.client.interceptors.request.use(
(config: AxiosRequestConfig<unknown>) => {
        const token = localStorage.getItem('token');
        if (token) {
// eslint-disable-next-line @typescript-eslint/no-explicit-any
config.headers = { ...(config.headers || {}), Authorization: `Bearer ${token}` } as any;
        }
return config as AxiosRequestConfig;
      },
      (error: AxiosError) => Promise.reject(error),
    );

    this.client.interceptors.response.use(
      (response: AxiosResponse) => response,
      (error: AxiosError) => {
        if (error.response?.status === 401 || error.response?.status === 403) {
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

  request<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<unknown>> {
    return this.client.request<T>(toAxiosConfig(url, config));
  }

  get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<unknown>> {
    return this.client.get<T>(normalizeApiPath(url), config);
  }

  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<AxiosResponse<unknown>> {
    return this.client.post<T>(normalizeApiPath(url), data, config);
  }

  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<AxiosResponse<unknown>> {
    return this.client.put<T>(normalizeApiPath(url), data, config);
  }

  delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<unknown>> {
    return this.client.delete<T>(normalizeApiPath(url), config);
  }
}

export const apiClient = new ApiClient();
export const apiGet = <T = unknown>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> => apiClient.get<T>(url, config);
export const apiPost = <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> => apiClient.post<T>(url, data, config);
export const apiPut = <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> => apiClient.put<T>(url, data, config);
export const apiDelete = <T = unknown>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> => apiClient.delete<T>(url, config);

export const apiFetch = async <T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> => {
  const response = await apiClient.request<T>(url, config);
  return response.data;
};

export default apiClient;

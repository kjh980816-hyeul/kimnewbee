import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  withCredentials: true,
  timeout: 10_000,
});

// access_token(httpOnly 쿠키)은 30분 만료. 만료 시 모든 인증요청이 401이 되는데,
// refresh_token(14일)이 있으면 /api/auth/refresh로 새 쿠키를 받아 원요청을 자동 재시도한다.
// 동시 401이 여러 개 떠도 refresh는 한 번만(single-flight) 호출한다.
let refreshPromise: Promise<void> | null = null;

async function refreshSession(): Promise<void> {
  // apiClient가 아닌 맨 axios로 호출해 인터셉터 재귀를 피한다.
  await axios.post('/api/auth/refresh', null, {
    baseURL: import.meta.env.VITE_API_URL,
    withCredentials: true,
  });
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as
      | (InternalAxiosRequestConfig & { _retry?: boolean })
      | undefined;
    const status = error.response?.status;
    const isAuthCall = (original?.url ?? '').includes('/api/auth/');

    if (status === 401 && original && !original._retry && !isAuthCall) {
      original._retry = true;
      try {
        if (!refreshPromise) {
          refreshPromise = refreshSession().finally(() => {
            refreshPromise = null;
          });
        }
        await refreshPromise;
        return apiClient(original);
      } catch {
        // refresh 실패(14일 만료/무효) → 원래 401을 그대로 전파해서 호출부가 로그인 유도
      }
    }
    return Promise.reject(error);
  },
);

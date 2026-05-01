import { apiClient } from './client';
import type { NaverLoginResponse } from '@/types/user';

export async function loginWithNaver(): Promise<NaverLoginResponse> {
  const res = await apiClient.post<NaverLoginResponse>('/api/auth/naver');
  return res.data;
}

export async function logout(): Promise<void> {
  await apiClient.post('/api/auth/logout');
}

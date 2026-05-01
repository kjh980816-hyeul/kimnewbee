import { apiClient } from './client';
import type { NaverLoginResponse } from '@/types/user';

export async function loginWithNaver(): Promise<NaverLoginResponse> {
  const res = await apiClient.post<NaverLoginResponse>('/api/auth/naver');
  return res.data;
}

import { apiClient } from './client';
import type { CurrentUser, UserStats } from '@/types/user';

export async function fetchMe(): Promise<CurrentUser> {
  const res = await apiClient.get<CurrentUser>('/api/me');
  return res.data;
}

export async function fetchMyStats(): Promise<UserStats> {
  const res = await apiClient.get<UserStats>('/api/me/stats');
  return res.data;
}

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

export async function updateMyProfileImage(profileImage: string | null): Promise<CurrentUser> {
  const res = await apiClient.patch<CurrentUser>('/api/me/profile-image', { profileImage });
  return res.data;
}

export async function updateMyNickname(nickname: string): Promise<CurrentUser> {
  const res = await apiClient.patch<CurrentUser>('/api/me/nickname', { nickname });
  return res.data;
}

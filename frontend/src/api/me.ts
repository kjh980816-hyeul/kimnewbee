import { apiClient } from './client';
import type { CurrentUser, MyActivityItem, UserStats } from '@/types/user';

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

export async function fetchMyPosts(): Promise<MyActivityItem[]> {
  const res = await apiClient.get<MyActivityItem[]>('/api/me/posts');
  return res.data;
}

export async function fetchMyCommentedPosts(): Promise<MyActivityItem[]> {
  const res = await apiClient.get<MyActivityItem[]>('/api/me/commented');
  return res.data;
}

export async function fetchMyLikedPosts(): Promise<MyActivityItem[]> {
  const res = await apiClient.get<MyActivityItem[]>('/api/me/liked');
  return res.data;
}

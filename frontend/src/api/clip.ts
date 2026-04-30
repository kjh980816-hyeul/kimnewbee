import { apiClient } from './client';
import type { Clip, ClipListResponse, CreateClipInput } from '@/types/clip';
import type { LikeToggleResponse } from '@/types/free';

export async function fetchClips(): Promise<ClipListResponse> {
  const res = await apiClient.get<ClipListResponse>('/api/clips');
  return res.data;
}

export async function fetchClip(id: number): Promise<Clip> {
  const res = await apiClient.get<Clip>(`/api/clips/${id}`);
  return res.data;
}

export async function createClip(input: CreateClipInput): Promise<Clip> {
  const res = await apiClient.post<Clip>('/api/clips', input);
  return res.data;
}

export async function toggleClipLike(id: number): Promise<LikeToggleResponse> {
  const res = await apiClient.post<LikeToggleResponse>(`/api/clips/${id}/like`);
  return res.data;
}

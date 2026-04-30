import { apiClient } from './client';
import type {
  Fanart,
  FanartListResponse,
  CreateFanartInput,
} from '@/types/fanart';
import type { LikeToggleResponse } from '@/types/free';

export async function fetchFanartList(): Promise<FanartListResponse> {
  const res = await apiClient.get<FanartListResponse>('/api/fanart');
  return res.data;
}

export async function fetchFanart(id: number): Promise<Fanart> {
  const res = await apiClient.get<Fanart>(`/api/fanart/${id}`);
  return res.data;
}

export async function createFanart(input: CreateFanartInput): Promise<Fanart> {
  const res = await apiClient.post<Fanart>('/api/fanart', input);
  return res.data;
}

export async function toggleFanartLike(id: number): Promise<LikeToggleResponse> {
  const res = await apiClient.post<LikeToggleResponse>(`/api/fanart/${id}/like`);
  return res.data;
}

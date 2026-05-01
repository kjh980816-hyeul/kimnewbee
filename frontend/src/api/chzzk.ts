import { apiClient } from './client';
import type { LiveStatus } from '@/types/chzzk';

export async function fetchLiveStatus(): Promise<LiveStatus> {
  const res = await apiClient.get<LiveStatus>('/api/chzzk/live');
  return res.data;
}

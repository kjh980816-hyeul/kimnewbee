import { apiClient } from './client';

export interface StatsSummary {
  totalMembers: number;
  todayNewPosts: number;
}

export async function fetchStatsSummary(): Promise<StatsSummary> {
  const res = await apiClient.get<StatsSummary>('/api/stats/summary');
  return res.data;
}

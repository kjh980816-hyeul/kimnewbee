import { apiClient } from './client';
import type { SearchResponse } from '@/types/search';

export async function search(q: string, type?: string): Promise<SearchResponse> {
  const res = await apiClient.get<SearchResponse>('/api/search', { params: { q, type } });
  return res.data;
}

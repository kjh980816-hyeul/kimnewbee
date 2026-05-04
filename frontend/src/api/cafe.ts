import { apiClient } from './client';
import type { CafeConfig, CafeConfigUpdateInput } from '@/types/cafe';

export async function fetchCafeConfig(): Promise<CafeConfig> {
  const res = await apiClient.get<CafeConfig>('/api/cafe/config');
  return res.data;
}

export async function updateCafeConfig(input: CafeConfigUpdateInput): Promise<CafeConfig> {
  const res = await apiClient.put<CafeConfig>('/api/admin/cafe/config', input);
  return res.data;
}
